package com.jetprobe.mongo.sink


import com.jetprobe.core.generator.Generator
import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.sink.DataSink
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.concurrent.duration._
import scala.concurrent.Await

/**
  * @author Shad.
  */
case class MongoSink private(val db: Expr, val collection: Expr, val host: Expr, config : Map[String,Any] = Map.empty)
  extends DataSink {

  import MongoSink._

  val batchSize = 512

  lazy val mongoClient = getMongoClient(host,config)

  override def save(record: Generator): Unit = {
    val collectionOpt = getCollectionOpt(host,db,collection,config)
    collectionOpt match {
      case Some(col) =>
        record
          .grouped(batchSize)
          .foreach(docs => {
            val observable =
              col.insertMany(docs.map(str => Document(BsonDocument(str))))
            Await.result(observable.head(), 10 seconds)
          })
        logger.info(s"Total docs inserted : ${record.length}")

      case None =>
        logger.error(s"Unable to fetch the collection for the ${collection.value}")
    }

    def closeClient : Unit = {
      mongoClient match {
        case Some(client) => client.close()
        case None => logger.warn("No Mongo client found to close.")
      }
    }


  }

}

object MongoSink {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  def apply(uri: String): MongoSink = {
    val splitUri = uri.substring(10).split("/")
    val hostname = "mongodb://" + splitUri(0)
    val database = if (splitUri.length > 1) Expr(splitUri(1)) else Expr("")
    val collection = if (splitUri.length > 2) Expr(splitUri(2)) else Expr("")
    MongoSink(database, collection, Expr(hostname))
  }

  def getMongoClient(host : Expr,config : Map[String,Any]) : Option[MongoClient] = {
    ExpressionParser.parse(host.value, config)
      .map(host => MongoClient(host))
  }

  /**
    * Get the collection post resolving the expression for collection
    * @param host
    * @param db
    * @param collection
    * @param config
    * @return
    */
  def getCollectionOpt(host : Expr, db : Expr, collection : Expr, config: Map[String, Any]): Option[MongoCollection[Document]] = {
    val parsedDb = getDatabaseOpt(host,db, config)
    parsedDb.flatMap { mdb =>
      ExpressionParser.parse(collection.value, config).map(coll => mdb.getCollection(coll))
    }
  }

  /**
    * Extract the database post parsing of the expression
    * @param host
    * @param db
    * @param config
    * @return
    */
  def getDatabaseOpt(host : Expr, db : Expr, config: Map[String, Any]): Option[MongoDatabase] = {
    ExpressionParser.parse(host.value, config)
      .map(host => MongoClient(host))
      .flatMap { client =>
        ExpressionParser.parse(db.value, config).map(resolvedDb => client.getDatabase(resolvedDb))
      }
  }

  def jsonStrToMap0(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }
}
