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
import sourcecode.{FullName, Line}


import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}


/**
  * @author Shad.
  */

class MongoValidator extends ValidationExecutor[MongoSink] with LazyLogging {


  override def execute(rules: Seq[ValidationRule[MongoSink]], sink: MongoSink, config: Map[String, Any]): Seq[ValidationResult] = {

    try {
      val client = sink.copy(config = config).mongoClient
      client match {
        case Some(mongoClient) =>
          rules.map {
            case rule: DatabaseStatsRule =>  handleDatabaseStatsRule(rule,config,mongoClient)
            case rule : MongoServerStatsRule => validateResponse[ServerStats](rule.fetch(mongoClient),rule.actual)
            case rule : MongoQueryRule[_] => rule.copy(mongoClient = Some(mongoClient)).validate(config)
          }

        case None => throw new Exception("Parser error for mongo client. Check the connection options.")

      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        rules.map(x => ValidationResult.skipped(x, ex.getMessage))
    }

  }



  private[this] def handleDatabaseStatsRule(dbRule: DatabaseStatsRule, config: Map[String, Any], client: MongoClient): ValidationResult = {

    val database = ExpressionParser.parse(dbRule.database.value, config)
    database match {

      case Some(db) =>
        val dbStatsInfo = dbRule.copy(database = Expr(db)).fetch(client)
        validateResponse[DBStats](dbStatsInfo, dbRule.actual)

      case None => ValidationResult.failed(dbRule, s"Unable to parse database expression : ${dbRule.database.value}")
    }

  }


}

object MongoValidator {


  private val separator = ">>"
  type ModelProperty = DataSource with Product with Serializable
  type ParsedResult[T] = Future[Either[Error, T]]

  private val serverStatsCommand = "{ serverStatus: 1 }"
  private val dbListCommand = "{ listDatabases: 1 }"
  val dbStatsCommand = "{dbStats: 1, scale: 1024 }"

  private def collectionStatsCommand(collection: String) = s"{collStats :\'${collection}\', scale: 1024, verbose : false}"


  val dbStats: mutable.Map[String, DBStats] = mutable.Map.empty
  val dbList: mutable.Map[String, DatabaseList] = mutable.Map.empty



  def getResult(db: MongoDatabase, cmd: String): Future[String] = {
    val mongoCmd = BsonDocument.parse(cmd)
    db.runCommand(mongoCmd).head().map { json =>
      // println(json.toJson())
      json.toJson()
    }

  }


  private[mongo] def runDocsValidation(rules: Seq[ValidationRule[MongoSink]],
                                       config: Map[String, Any], host: Expr): Seq[ValidationResult] = rules.filter(_.isInstanceOf[DocumentsRule[_]]).map {
    case docRule: DocumentsRule[_] =>
      val exprs = Seq(host, docRule.db, docRule.collection, docRule.query)
      val parsedExprs = parseAll(exprs, config)
      try {
        if (parsedExprs.values.toList.count(_.nonEmpty) == exprs.size) {
          val docValidator = new BsonDocValidator(parsedExprs(host.value).get, parsedExprs(docRule.db.value).get, parsedExprs(docRule.collection.value).get)
          docValidator.validateDocs(docRule, parsedExprs(docRule.query.value).get, config)
        } else {
          ValidationResult.skipped(docRule, s"Unable to parse the doc rule with findQuery = ${docRule.query.value}")
        }
      }
      catch {
        case ex: Exception =>
          ex.printStackTrace()
          ValidationResult.skipped(docRule, ex.getMessage)
      }

  }

  import ExpressionParser.parse

  private[mongo] def parseAll(expressions: Seq[Expr], config: Map[String, Any]): Map[String, Option[String]] = {
    expressions.map(expr => expr.value -> parse(expr.value, config)).toMap
  }

  /*private[mongo] def runDBListValidation(dbList: DatabaseList, dbListRule: DatabaseListRule[_]): ValidationResult = {

    dbListRule.actual(dbList) == dbListRule.expected match {
      case true => ValidationResult.success(dbListRule)
      case _ =>
        val failureMessage = getFailureMessage(dbListRule.name, dbListRule.actual(dbList),
          dbListRule.expected, dbListRule.fullName.value, dbListRule.line.value)
        ValidationResult.failed(dbListRule, failureMessage)
    }

  }*/
}