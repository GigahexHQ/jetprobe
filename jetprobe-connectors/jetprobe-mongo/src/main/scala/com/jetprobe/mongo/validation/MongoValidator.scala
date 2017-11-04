package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.sink.DataSource
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.mongo.models.{CollectionStats, DBStats, DatabaseList, ServerStats}
import com.jetprobe.mongo.sink.MongoSink
import com.typesafe.scalalogging.LazyLogging
import org.bson.BsonDocument
import org.mongodb.scala.{MongoClient, MongoDatabase}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

import concurrent.duration._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}


/**
  * @author Shad.
  */
class MongoValidator extends ValidationExecutor[MongoSink] with LazyLogging {

  import MongoValidator._


  override def execute(rules: Seq[ValidationRule[MongoSink]], sink: MongoSink, config: Map[String, Any]): Seq[ValidationResult] = {

    try {
      val client = sink.copy(config = config).mongoClient
      client match {
        case Some(mongoClient) =>

          lazy val serverStats = getResult(mongoClient.getDatabase("admin"), serverStatsCommand).map(json => decode[ServerStats](json))
          lazy val dblist = getResult(mongoClient.getDatabase("admin"), dbListCommand).map(json => decode[DatabaseList](json))
          val dbStatsResults = extractDBStatsRules(rules, config, mongoClient)
          val collectionStatsResults = extractCollectionStatsRules(rules, config, mongoClient)
          val docsValidationResult = runDocsValidation(rules, config, sink.host)
          runCommonValidation(rules, serverStats, dblist, dbStatsResults, collectionStatsResults) ++ docsValidationResult

        case None => throw new Exception("Parser error for mongo client. Check the config.")
      }
    } catch {
      case ex: Exception => rules.map(x => ValidationResult.skipped(x,ex.getMessage))
    }
  }
}

object MongoValidator {

  import com.jetprobe.core.validations.ValidationHelper._

  private val separator = ">>"
  type ModelProperty = DataSource with Product with Serializable
  type ParsedResult[T] = Future[Either[Error, T]]

  private val serverStatsCommand = "{ serverStatus: 1 }"
  private val dbListCommand = "{ listDatabases: 1 }"
  private val dbStatsCommand = "{dbStats: 1, scale: 1024 }"

  private def collectionStatsCommand(collection: String) = s"{collStats :\'${collection}\', scale: 1024, verbose : false}"


  val dbStats: mutable.Map[String, DBStats] = mutable.Map.empty
  val dbList: mutable.Map[String, DatabaseList] = mutable.Map.empty

  /**
    *
    * @param rules
    * @return
    */
  private[mongo] def extractDBStatsRules(rules: Seq[ValidationRule[MongoSink]],
                                         config: Map[String, Any], mongoClient: MongoClient): Map[String, Option[ParsedResult[DBStats]]] = {
    val dbStatsRules = rules.filter(x => x.isInstanceOf[DBStatsRule[_]]).map(_.asInstanceOf[DBStatsRule[_]]).groupBy {
      case k: DBStatsRule[_] => k.database.value
    }
    dbStatsRules.map {
      case (dbExpr, _) =>
        val database = ExpressionParser.parse(dbExpr, config)
        database match {
          case Some(parsedDB) => dbExpr -> Some(getResult(mongoClient.getDatabase(dbExpr), dbStatsCommand).map(json => decode[DBStats](json)))
          case None => dbExpr -> None
        }
    }
  }

  private[mongo] def extractCollectionStatsRules(rules: Seq[ValidationRule[MongoSink]],
                                                 config: Map[String, Any],
                                                 mongoClient: MongoClient): Map[(String, String), Option[ParsedResult[CollectionStats]]] = {
    val collectionStatsRules = rules.filter(_.isInstanceOf[CollectionStatsRule[_]]).map(_.asInstanceOf[CollectionStatsRule[_]]).groupBy {
      rule =>
        (rule.db, rule.collection) match {
          case (Expr(db), Expr(coll)) => (db, coll)
        }
    }
    collectionStatsRules.map {
      case ((db, coll), _) =>
        (ExpressionParser.parse(db, config), ExpressionParser.parse(coll, config)) match {
          case (Some(dbFound), Some(collFound)) =>
            val result = Some(getResult(mongoClient.getDatabase(db), collectionStatsCommand(coll)).map(decode[CollectionStats](_)))
            (db, coll) -> result
          case _ => (db, coll) -> None
        }
    }

  }


  def getResult(db: MongoDatabase, cmd: String): Future[String] = {
    val mongoCmd = BsonDocument.parse(cmd)
    db.runCommand(mongoCmd).head().map { json =>
      // println(json.toJson())
      json.toJson()
    }

  }

  def getServerStats(db: MongoDatabase): Future[Either[Error, ServerStats]] = {
    println("Connecting to the db server for getting stats ")
    val cmd = BsonDocument.parse("{ serverStatus: 1 }")

    db.runCommand(cmd)
      .head()
      .map(doc => decode[ServerStats](doc.toJson()))
  }

  def getDatabaseList(db: MongoDatabase): Future[Either[Error, DatabaseList]] = {
    val cmd = BsonDocument.parse("{ listDatabases: 1 }")

    db.runCommand(cmd)
      .head()
      .map(doc => decode[DatabaseList](doc.toJson()))
  }

  def validate[T, U](meta: Future[Either[Exception, T]], rule: ValidationRule[_])
                    (validator: (T, ValidationRule[_]) => ValidationResult): Future[ValidationResult] = {
    meta.map {
      case Left(error) => ValidationResult.skipped(rule,error.getMessage)
      case Right(fetchedMeta) => validator(fetchedMeta, rule)
    }
  }

  /**
    *
    * @param meta Property model of the component that is being validated
    * @param rule The validation Rule
    * @tparam T The type of the data model that is being validated
    * @tparam U The type of the validation rule
    * @return
    */
  def runValidation[T, U](meta: Future[Either[Error, T]], rule: ValidationRule[_]): Future[ValidationResult] = {
    meta.map {
      case Left(error) => ValidationResult.skipped(rule,error.getMessage)
      case Right(fetchedMeta) =>
        fetchedMeta match {
          case ss: ServerStats => runServerStatsValidator(ss, rule.asInstanceOf[ServerStatsRule[_]])
          case ds: DBStats => runDbStatsValidation(ds, rule.asInstanceOf[DBStatsRule[_]])
          case dbList: DatabaseList => runDBListValidation(dbList, rule.asInstanceOf[DatabaseListRule[_]])
          case collStats: CollectionStats => runCollStatsValidation(collStats, rule.asInstanceOf[CollectionStatsRule[_]])

        }

    }
  }

  private[mongo] def runCollStatsValidation(collStats: CollectionStats, rule: CollectionStatsRule[_]): ValidationResult = {
    rule.actual(collStats) == rule.expected match {
      case true => ValidationResult.success(rule)
      case false =>
        val failureMessage = rule.onFailure(rule.actual(collStats), rule.expected)
        ValidationResult.failed(rule,failureMessage)
    }
  }

  private[mongo] def runDbStatsValidation(dBStats: DBStats, rule: DBStatsRule[_]): ValidationResult = {
    rule.actual(dBStats) == rule.expected match {
      case true => ValidationResult.success(rule)
      case false =>
        val failureMessage = getFailureMessage(rule.name, rule.actual(dBStats),
          rule.expected, rule.fullName.value, rule.line.value)
        ValidationResult.failed(rule,failureMessage)
    }
  }

  private[mongo] def runServerStatsValidator(serverStats: ServerStats, statsRule: ServerStatsRule[_]): ValidationResult = {

    val expected = statsRule.expected
    val actual = statsRule.actual(serverStats)
    if (expected == actual) {
      ValidationResult.success(statsRule)
    }
    else {
      val failureMessage = s"${statsRule.name} failed at ${statsRule.fullName.value} : ${statsRule.line.value}. Expected = $expected , Actual = $actual"
      ValidationResult.failed(statsRule,failureMessage)
    }
  }

  private[mongo] def runCommonValidation(rules: Seq[ValidationRule[MongoSink]],
                                         ss: ParsedResult[ServerStats],
                                         dblist: ParsedResult[DatabaseList],
                                         dbStatsRes: Map[String, Option[ParsedResult[DBStats]]],
                                         dbCollRes: Map[(String, String), Option[ParsedResult[CollectionStats]]]): Seq[ValidationResult] = {
    val futureValidation = rules.filterNot(_.isInstanceOf[DocumentsRule[_]]) map {
      case validationRule: ServerStatsRule[_] => runValidation[ServerStats, ServerStatsRule[_]](ss, validationRule)
      case dbListRule: DatabaseListRule[_] => runValidation[DatabaseList, DatabaseListRule[_]](dblist, dbListRule)
      case dbStatRule: DBStatsRule[_] => dbStatsRes.getOrElse(dbStatRule.database.value, None) match {
        case Some(dbStatsResult) => runValidation[DBStats, DBStatsRule[_]](dbStatsResult, dbStatRule)
        case None => Future(ValidationResult.skipped(dbStatRule, s"Parser error for database : ${dbStatRule.database.value}"))
      }
      case dbCollRule: CollectionStatsRule[_] => dbCollRes.getOrElse((dbCollRule.db.value, dbCollRule.collection.value), None) match {
        case Some(collResult) => runValidation[CollectionStats, CollectionStatsRule[_]](collResult, dbCollRule)
        case None => Future(
          ValidationResult.skipped(dbCollRule, s"Parser error for database : ${dbCollRule.db.value} & collection : ${dbCollRule.collection.value}")
        )
      }

    }
    val validationResults = Future.sequence(futureValidation)
    Await.result(validationResults, 60.seconds)
  }

  private[mongo] def runDocsValidation(rules: Seq[ValidationRule[MongoSink]],
                                       config: Map[String, Any], host: Expr): Seq[ValidationResult] = rules.filter(_.isInstanceOf[DocumentsRule[_]]).map {
    case docRule: DocumentsRule[_] =>
      val exprs = Seq(host, docRule.db, docRule.collection, docRule.query)
      val parsedExprs = parseAll(exprs, config)
      if (parsedExprs.values.toList.count(_.nonEmpty) == exprs.size) {
        val docValidator = new BsonDocValidator(parsedExprs(host.value).get, parsedExprs(docRule.db.value).get, parsedExprs(docRule.collection.value).get)
        docValidator.validateDocs(docRule, parsedExprs(docRule.query.value).get, config)
      } else {
        ValidationResult.skipped(docRule, s"Unable to parse the doc rule with findQuery = ${docRule.query.value}")
      }
  }

  import ExpressionParser.parse

  private[mongo] def parseAll(expressions: Seq[Expr], config: Map[String, Any]): Map[String, Option[String]] = {
    expressions.map(expr => expr.value -> parse(expr.value, config)).toMap
  }

  private[mongo] def runDBListValidation(dbList: DatabaseList, dbListRule: DatabaseListRule[_]): ValidationResult = {

    dbListRule.actual(dbList) == dbListRule.expected match {
      case true => ValidationResult.success(dbListRule)
      case _ =>
        val failureMessage = getFailureMessage(dbListRule.name, dbListRule.actual(dbList),
          dbListRule.expected, dbListRule.fullName.value, dbListRule.line.value)
        ValidationResult.failed(dbListRule,failureMessage)
    }

  }
}