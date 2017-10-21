package com.jetprobe.mongo.validation

import com.jetprobe.core.sink.DataSource
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.mongo.models.{CollectionStats, DBStats, DatabaseList, ServerStats}
import com.jetprobe.mongo.sink.MongoSink
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
class MongoValidator extends ValidationExecutor[MongoSink] {

  import MongoValidator._

  val separator = ">>"

  override def execute(rules: Seq[ValidationRule[MongoSink]], sink: MongoSink): Seq[ValidationResult] = {

    try {
      val mongoClient = MongoClient(sink.host)
      lazy val serverStats = getResult(mongoClient.getDatabase("admin"), serverStatsCommand).map(json => decode[ServerStats](json))

      lazy val dblist = getResult(mongoClient.getDatabase("admin"), dbListCommand).map(json => decode[DatabaseList](json))


      val dbStatsRules = rules.filter(x => x.isInstanceOf[DBStatsRule[_]]).groupBy {
        case k: DBStatsRule[_] => k.database
      }

      val collectionStatsRules = rules.filter(_.isInstanceOf[CollectionStatsRule[_]]).groupBy {
        r =>
          val rule = r.asInstanceOf[CollectionStatsRule[_]]
          rule.db + separator + rule.collection
      }
      val collectionStatResults = collectionStatsRules.map {
        case (dbColl, _) =>
          val db = dbColl.split(separator)(0)
          val coll = dbColl.split(separator)(1)
          val res = getResult(mongoClient.getDatabase(db), collectionStatsCommand(coll)).map(decode[CollectionStats](_))
          dbColl -> res
      }

      lazy val dbStatResults = dbStatsRules.map {
        case (db, _) => db -> getResult(mongoClient.getDatabase(db), dbStatsCommand).map(json => decode[DBStats](json))
      }

      val docsValidationResult = rules.filter(_.isInstanceOf[DocumentsRule[_]]).map {
        case docRule: DocumentsRule[_] =>
          val docValidator = new BsonDocValidator(sink, docRule.db, docRule.collection)
          docValidator.validateDocs(docRule)
      }

      val futureValidation = rules.filterNot(_.isInstanceOf[DocumentsRule[_]]) map {
        case validationRule: ServerStatsRule[_] => runValidation[ServerStats, ServerStatsRule[_]](serverStats, validationRule)
        case dbListRule: DatabaseListRule[_] => runValidation[DatabaseList, DatabaseListRule[_]](dblist, dbListRule)
        case dbStatRule: DBStatsRule[_] => runValidation[DBStats, DBStatsRule[_]](dbStatResults.get(dbStatRule.database).get, dbStatRule)
        case collStatsRule: CollectionStatsRule[_] =>
          runValidation[CollectionStats, CollectionStatsRule[_]](
            collectionStatResults(collStatsRule.db + separator + collStatsRule.collection), collStatsRule)
      }
      val validationResults = Future.sequence(futureValidation)
      Await.result(validationResults, 60.seconds) ++ docsValidationResult
    } catch {
      case ex: Exception => rules.map(x => ValidationResult(false,None,Some("Validaiton failed : " + ex.getMessage)))
    }
  }
}

object MongoValidator {

  import com.jetprobe.core.validations.ValidationHelper._

  type ModelProperty = DataSource with Product with Serializable

  private val serverStatsCommand = "{ serverStatus: 1 }"
  private val dbListCommand = "{ listDatabases: 1 }"
  private val dbStatsCommand = "{dbStats: 1, scale: 1024 }"

  private def collectionStatsCommand(collection: String) = s"{collStats :\'${collection}\', scale: 1024, verbose : false}"


  val dbStats: mutable.Map[String, DBStats] = mutable.Map.empty
  val dbList: mutable.Map[String, DatabaseList] = mutable.Map.empty

  def getResult(db: MongoDatabase, cmd: String): Future[String] = {
    val mongoCmd = BsonDocument.parse(cmd)
    db.runCommand(mongoCmd).head().map{ json =>
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
      case Left(error) => ValidationResult(false, None, Some(s"Validation Failed. Cause : ${error.getMessage}"))
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
  def runValidation[T, U](meta: Future[Either[Error, T]], rule: U): Future[ValidationResult] = {
    meta.map {
      case Left(error) => ValidationResult(false, None, Some(s"Validation Failed. Cause : ${error.getMessage}"))
      case Right(fetchedMeta) =>
        fetchedMeta match {
          case ss: ServerStats =>
            val r = rule.asInstanceOf[ServerStatsRule[_]]
            r.actual(ss) == r.expected match {
              case true => ValidationResult(true, Some(r.onSuccess), None)
              case _ =>
                val failureMessage = getFailureMessage(r.name, r.actual(ss),
                  r.expected, r.fullName.value, r.line.value)
                ValidationResult(false, None, Some(failureMessage))
            }
          case ds: DBStats => runDbStatsValidation(ds, rule.asInstanceOf[DBStatsRule[_]])
          case dbList: DatabaseList => runDBListValidation(dbList, rule.asInstanceOf[DatabaseListRule[_]])
          case collStats: CollectionStats => runCollStatsValidation(collStats, rule.asInstanceOf[CollectionStatsRule[_]])

        }

    }
  }

  private[mongo] def runCollStatsValidation(collStats: CollectionStats, rule: CollectionStatsRule[_]): ValidationResult = {
    rule.actual(collStats) == rule.expected match {
      case true => ValidationResult(true, Some(rule.onSuccess), None)
      case false =>
        val failureMessage = rule.onFailure(rule.actual(collStats), rule.expected)
        ValidationResult(false, None, Some(failureMessage))
    }
  }

  private[mongo] def runDbStatsValidation(dBStats: DBStats, rule: DBStatsRule[_]): ValidationResult = {
    rule.actual(dBStats) == rule.expected match {
      case true => ValidationResult(true, Some(rule.onSuccess), None)
      case false =>
        val failureMessage = getFailureMessage(rule.name, rule.actual(dBStats),
          rule.expected, rule.fullName.value, rule.line.value)
        ValidationResult(false, None, Some(failureMessage))
    }
  }

  def runServerStatsValidator(serverStats: ServerStats, statsRule: ServerStatsRule[_]): ValidationResult = {

    val expected = statsRule.expected
    val actual = statsRule.actual(serverStats)
    if (expected == actual) {
      ValidationResult(true, Some(statsRule.onSuccess), None)
    }
    else {
      val failureMessage = s"${statsRule.name} failed at ${statsRule.fullName.value} : ${statsRule.line.value}. Expected = $expected , Actual = $actual"
      ValidationResult(false, None, Some(failureMessage))
    }


  }

  private[mongo] def runDBListValidation(dbList: DatabaseList, dbListRule: DatabaseListRule[_]): ValidationResult = {

    dbListRule.actual(dbList) == dbListRule.expected match {
      case true => ValidationResult(true, Some(dbListRule.onSuccess), None)
      case _ =>
        val failureMessage = getFailureMessage(dbListRule.name, dbListRule.actual(dbList),
          dbListRule.expected, dbListRule.fullName.value, dbListRule.line.value)
        ValidationResult(false, None, Some(failureMessage))
    }

  }
}