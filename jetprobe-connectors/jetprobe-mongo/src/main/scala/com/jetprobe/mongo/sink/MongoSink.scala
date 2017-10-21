package com.jetprobe.mongo.sink


import com.jetprobe.core.generator.Generator
import com.jetprobe.core.sink.DataSink
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.concurrent.duration._
import scala.concurrent.Await

/**
  * @author Shad.
  */
class MongoSink(val db: String, val collection: String, val host: String)
  extends DataSink {

  import MongoSink._

  override def save(record: Generator): Unit = {
    val collection = getCollection(this)

    record
      .grouped(1000)
      .foreach(docs => {
        val observable =
          collection.insertMany(docs.map(str => Document(BsonDocument(str))))
        Await.result(observable.head(), 10 seconds)
      })
    logger.info(s"Total docs inserted : ${record.length}")
  }

}

object MongoSink {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  def apply(uri: String): MongoSink = {
    val splitUri = uri.substring(10).split("/")
    val hostname = "mongodb://" + splitUri(0)
    val database = splitUri(1)
    val collection = splitUri(2).split("\\?")(0)
    new MongoSink(database, collection, hostname)
  }

  def getCollection(mongo: MongoSink): MongoCollection[Document] = {
    val mongoClient = MongoClient(mongo.host)
    mongoClient.getDatabase(mongo.db).getCollection(mongo.collection)
  }

  def jsonStrToMap0(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }
}
