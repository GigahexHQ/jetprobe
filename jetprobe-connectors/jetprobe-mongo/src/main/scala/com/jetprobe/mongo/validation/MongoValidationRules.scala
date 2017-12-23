package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser, ParsedSelect, SQLParser}
import com.jetprobe.core.validations.ValidationRule.ActualResolver
import com.jetprobe.core.validations.{RuleValidator, ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.mongo.models._
import com.jetprobe.mongo.sink.MongoSink
import io.circe.generic.auto._
import io.circe.Error
import io.circe.parser.decode
import org.mongodb.scala.MongoClient
import org.mongodb.scala.model.Projections

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}


/**
  * @author Shad.
  */

trait MongoDBPropertyFetcher[T] {

  def fetch(client: MongoClient): Future[Either[Error, T]]

}

case class MongoServerStatsRule(actual: (ServerStats) => Any) extends ValidationRule[MongoSink] with MongoDBPropertyFetcher[ServerStats] {

  private val serverStatsCommand = "{ serverStatus: 1 }"


  override def name: String = "MongoDB Server validation "

  override def fetch(client: MongoClient): Future[Either[Error, ServerStats]] = {
    val result = MongoValidator.getResult(client.getDatabase("admin"), serverStatsCommand)
    result.map(json => decode[ServerStats](json))
  }

}


case class DatabaseStatsRule(actual: (DBStats) => Any, database: Expr) extends ValidationRule[MongoSink] with MongoDBPropertyFetcher[DBStats] {

  val dbStatsCommand = "{dbStats: 1, scale: 1024 }"


  override def name: String = s"Validations for database : ${database.value}"

  override def fetch(client: MongoClient): Future[Either[Error, DBStats]] = {
    val result = MongoValidator.getResult(client.getDatabase(database.value), dbStatsCommand)
    result.map(json => decode[DBStats](json))
  }
}


case class CollectionStatsRule[U <: Any](expected: U,
                                         actual: ActualResolver[CollectionStats],
                                         db: Expr = Expr(),
                                         collection: Expr = Expr())
  extends ValidationRule[MongoSink] {

  override def name: String = s"Stats validation for collection : ${collection.value} in database : ${db.value}"


}

case class MongoQueryRule[T](validationFn : Seq[T] => Any, query : Expr, decoder : String => T, mongoClient : Option[MongoClient])
  extends ValidationRule[MongoSink] with RuleValidator {

  override def name: String = s"Query validation "


  def fetch(mongoClient: MongoClient,config : Map[String,Any]) : Future[Either[Exception,Seq[T]]] = {

    var parsedVars : Map[String,String] = Map.empty

    val parsedSQL = ExpressionParser.parseAll(Seq(query),config) match {
      case Left(ex) => None
      case Right(mp) =>
        val parser = new SQLParser
        parsedVars = mp
        parser.parse(mp(query.value)).map(parser.extract(_))
    }

    parsedSQL match {
      case Some(sql) =>
        val schema = sql.table.split("\\.")
        val dbName = schema(0)
        val collection = mongoClient.getDatabase(dbName).getCollection(schema(1))
        val records : ListBuffer[T]  = ListBuffer.empty

        val result = collection.find().projection(Projections.include(sql.fields :_*)).foldLeft(records){
          case (x,y) =>
            val casted = decoder(y.toJson())
            records.+=(casted)
        }

        result.head().map(Right(_))
      case None =>
        Future(Left(new Exception("Unable to parse SQL Expression.")))


    }

  }

  override def validate(config: Map[String, Any]): ValidationResult = {

    val results = fetch(mongoClient.get,config)
    validateResponse[Seq[T]](results,validationFn)

  }

}

case class DocumentsRule[U](expected: U,
                            actual: (SourceBsonDocuments[Any]) => U,
                            db: Expr = Expr(),
                            collection: Expr = Expr(),
                            query: Expr = Expr())
  extends ValidationRule[MongoSink] {

  override def name: String = s"Docs validation for collection : $collection in database : $db"


}

/** **********************
  * Models for Query
  * **********************/

class DBQuery(val name: String)

class ServerQuery

class CollectionQuery(val database: String, val collection: String)

class DocQuery(val database: String, val collection: String, val query: String)

class MongoSQL[T](val sql : String, val decoder : String => T)

trait MongoConditionalQueries {

  def server : ServerQuery = new ServerQuery

  def having(database: String): DBQuery = new DBQuery(database)

  def having(database: String, collection: String) = new CollectionQuery(database, collection)

  def having(database: String, collection: String, query: String) = new DocQuery(database, collection, query)

  def select[T](sql : String)( implicit decoder : String => T) = new MongoSQL(sql,decoder)

}




