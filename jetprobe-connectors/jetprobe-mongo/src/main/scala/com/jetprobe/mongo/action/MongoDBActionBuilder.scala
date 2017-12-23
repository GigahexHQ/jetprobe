package com.jetprobe.mongo.action

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.jetprobe.core.Predef.Session
import com.jetprobe.core.action.{Action, ActionMessage, ExecutableAction, SelfExecutableAction}
import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.mongo.sink.MongoSink
import com.mongodb.client.model.IndexModel
import com.mongodb.client.result.DeleteResult
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.{Completed, MongoCollection}

/**
  * @author Shad.
  */
class MongoDBActionBuilder(actionDef : MongoIOActionDef,mongoSink : MongoSink) extends ActionBuilder{

  private val batchSize = 512
  private val idxStr = """{ "idx" : 1 }"""

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {

    new SelfExecutableAction("MongoDBAction",actionDef,next,ctx.system,ctx.controller)(handleActionMessage)

  }

  private[this] def handleActionMessage(message : ActionMessage,session: Session) : Session = {

    val mongoClient = mongoSink.copy(config = session.attributes).mongoClient
    if(mongoClient.nonEmpty){
      message match {

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

    session

  }

}


