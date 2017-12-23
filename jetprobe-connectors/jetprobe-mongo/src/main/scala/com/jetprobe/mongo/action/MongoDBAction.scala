package com.jetprobe.mongo.action

import akka.actor.{PoisonPill, Props}
import com.jetprobe.core.action.{Action, ActionActor, ActionMessage}
import com.jetprobe.core.session.Session
import com.jetprobe.mongo.sink.MongoSink
import com.mongodb.client.model.IndexModel
import com.mongodb.client.result.DeleteResult
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.{Completed, MongoClient, MongoCollection}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Shad.
  */
/*class MongoIOActor(val next: Action, mongoSink: MongoSink) extends ActionActor {

  val batchSize = 512
  val idxStr = """{ "idx" : 1 }"""

  override def execute(actionMessage: ActionMessage, session: Session): Unit = {

    val mongoClient = mongoSink.copy(config = session.attributes).mongoClient
    if(mongoClient.nonEmpty){
      actionMessage match {

        case CreateDatabase(db) =>
          //Need to create temp collection so that database exists.
          mongoClient.get.getDatabase(db).createCollection("tmp").subscribe((c: Completed) => logger.info(s"tmp collection created."))

        case CreateCollection(db, collection,indexFields) =>
          mongoClient.get.getDatabase(db).createCollection(collection).subscribe((c: Completed) => logger.info(s"collection ${collection} created."))
          if(indexFields.nonEmpty){
            val ccoll = mongoClient.get.getDatabase(db).getCollection(collection)
            indexFields.foreach{indx =>
              val indexs = indexFields.map(idx => new IndexModel(BsonDocument(idxStr.replace("idx",idx))))
              ccoll.createIndexes(indexs).subscribe((s : String) => logger.info(s"index ${s} created."))
            }
          }

        case DropCollection(db, collection) =>
          mongoClient.get.getDatabase(db).getCollection(collection)
            .drop()
            .subscribe((c: Completed) => logger.info(s"collection ${collection} dropped."))

        case RemoveAllDocuments(db, collection) =>
          mongoClient.get.getDatabase(db).getCollection(collection)
            .deleteMany(BsonDocument("{}"))
            .subscribe((result: DeleteResult) => logger.info(s"collection ${collection} truncated. ${result.getDeletedCount} rows deleted."))

        case InsertRows(db, coll, rows) =>
          val collection: MongoCollection[Document] = mongoClient.get.getDatabase(db).getCollection(coll)
          logger.info(s"Persisting in batches of size : ${batchSize}")
          var batchId = 0
          rows
            .grouped(batchSize)
            .foreach(docs => {
              val observable = collection.insertMany(docs.map(d => Document(BsonDocument(d))))
                .subscribe {
                  (c: Completed) =>
                    batchId += 1
                    logger.info(s"Persisted batch #${batchId}")
                }

            })

          logger.info("Perisistence completed.")
      }

    } else {
      logger.error(s"Unable to build the mongo client for host : ${mongoSink.host.value}")

    }
    next ! session

  }


}*/

sealed trait MongoIOActionDef extends ActionMessage {
  override def name: String = this.toString
}


case class CreateCollection(database: String, collection: String, indexFields : Seq[String] = Seq.empty) extends MongoIOActionDef

case class InsertRows(database: String, collection: String, rows: Iterator[String]) extends MongoIOActionDef

case class SelectQuery(q : String) extends MongoIOActionDef

case class CreateDatabase(database: String) extends MongoIOActionDef

case class DropDatabase(db: String) extends MongoIOActionDef

case class DropCollection(database: String, collection: String) extends MongoIOActionDef

case class RemoveAllDocuments(database: String, collection: String) extends MongoIOActionDef


/*
object MongoIOActor {


  def props(next: Action, mongoSink: MongoSink): Props = Props(new MongoIOActor(next, mongoSink))
}

*/



