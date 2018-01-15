package com.jetprobe.mongo.validation

import com.jetprobe.core.parser._
import com.jetprobe.core.validations.{RuleValidator, ValidationResult, ValidationRule}
import com.jetprobe.mongo.storage.MongoStorage
import io.circe.Error
import org.mongodb.scala.MongoClient
import org.mongodb.scala.model.Projections
import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}


/**
  * @author Shad.
  */

trait MongoDBPropertyFetcher[T] {

  def fetch(client: MongoClient): Future[Either[Error, T]]

}


case class MongoQueryRule[T](validationFn : Seq[T] => Any, query : Expr, decoder : String => T, mongoClient : Option[MongoClient])
  extends ValidationRule[MongoStorage] with RuleValidator with SqlExecutor {

  override def name: String = s"Query validation "

  def queryHandler(sqlStmt: SelectStmt,storage: MongoStorage) : Seq[String] = {

    val filterOption = QueryBuilder.buildFilter(sqlStmt)
    val (db,collection) = QueryBuilder.getTable(sqlStmt)

    val executionResult = storage.usingMongoClient {client =>
      val collectionInstance = client.getDatabase(db).getCollection(collection)
      val fieldNames = QueryBuilder.getProjections(sqlStmt.projections)
      val records : ArrayBuffer[String] = ArrayBuffer.empty
      val result = filterOption match {
        case Some(filter) =>
          collectionInstance.find(filter).projection(Projections.include(fieldNames :_*))
        case None =>
          collectionInstance.find().projection(Projections.include(fieldNames :_*))

      }
      result.foldLeft(records){
        case (x,y) =>
          val casted = y.toJson()
          records += casted
      }
    }

    executionResult match {
      case Some(res) => Await.result(res.head(),10.seconds)
      case None => throw new Exception("Unable to execute query")
    }

  }




  def fetch(mongoClient: MongoClient,config : Map[String,Any],storage: MongoStorage) : Future[Either[Throwable,Seq[T]]] = {

    execute(query.value,config,storage,queryHandler,decoder)


  }

  override def validate(config: Map[String, Any], storage: MongoStorage): ValidationResult = {
    val results = fetch(mongoClient.get,config,storage)
    validateResponse[Seq[T]](results,validationFn,this)
  }


}


/** **********************
  * Models for Query
  * **********************/

class MongoSQL[T](val sql : String, val decoder : String => T)

trait MongoConditionalQueries {

  def select[T](sql : String)( implicit decoder : String => T) = new MongoSQL(sql,decoder)

}




