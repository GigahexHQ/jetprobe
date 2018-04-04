package com.jetprobe.mongo.storage

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.Config
import org.json4s._
import com.jetprobe.mongo.validation.MongoConditionalQueries
import com.mongodb.client.model.IndexModel
import com.mongodb.client.result.DeleteResult
import scala.concurrent.ExecutionContext.Implicits.global
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


/**
  * @author Shad.
  */
case class MongoStorage private[jetprobe](uri: String, conf: Map[String, Any])
  extends Storage with MongoConditionalQueries {

  implicit val formats = DefaultFormats

  val batchSize = 512
  val idxStr = """{ "idx" : 1 }"""
  private val serverStatsCommand = "{ serverStatus: 1 }"
  private val dbListCommand = "{ listDatabases: 1 }"
  val dbStatsCommand = "{dbStats: 1, scale: 1024 }"

  private def collectionStatsCommand(collection: String) = s"{collStats :\'${collection}\', scale: 1024, verbose : false}"


  lazy private val mongoClient: MongoClient = MongoClient(uri)

  def load(records: Iterator[String], database: String, collectionName: String): Unit = {

    ExpressionParser.parseAll(Seq(Expr(database), Expr(collectionName)), conf) match {
      case Left(ex) => throw ex
      case Right(mappedVals) =>
        val mongoCollection: MongoCollection[Document] = mongoClient.getDatabase(mappedVals(database)).getCollection(mappedVals(collectionName))
        records
          .grouped(batchSize)
          .foreach(docs => {
            val observable = mongoCollection.insertMany(docs.map(str => Document(BsonDocument(str))))
            Await.result(observable.head(), 100 seconds)
          })
    }
  }

  private def getResult(db: MongoDatabase, cmd: String): Future[Document] = {
    val mongoCmd = org.bson.BsonDocument.parse(cmd)
    db.runCommand(mongoCmd).head().map( d => new Document(d.toBsonDocument))
  }

  def getDatabaseStats(db : String) : Option[Document] = {
    usingDatabase(db) {d =>
      val futureResult = getResult(d,dbStatsCommand)
      Await.result(futureResult,100.seconds)
    }
  }

  /**
    * Create collection with the given name
    *
    * @param db
    * @param collection
    * @param indexFields
    */
  def createCollection(db: String, collection: String, indexFields: Seq[String] = Seq.empty): Unit = {
    usingDatabase(db) { rdb =>
      ExpressionParser.parse(collection, conf) map { c =>
        rdb.createCollection(collection).subscribe((c: Completed) => logger.info(s"collection ${collection} created."))
        val col: MongoCollection[Document] = rdb.getCollection(c)

        if (indexFields.nonEmpty) {
          val indexs = indexFields.map(idx => new IndexModel(BsonDocument(idxStr.replace("idx", idx))))
          col.createIndexes(indexs).subscribe((s: String) => logger.info(s"index ${s} created."))
        }

      }


    }
  }

  /**
    * Drop the database from Mongo
    *
    * @param db An expression/value representing the database name
    */
  def dropDatabase(db: String): Unit = {
    usingDatabase(db) { rdb =>
      rdb.drop().subscribe(
        {
          (c: Completed) => logger.info(s"Database ${rdb.name} dropped.")
        }
      )
    }
  }

  /**
    * Drops the collection in the specified database
    *
    * @param db             database name
    * @param collectionName collection to be dropped
    */
  def dropCollection(db: String, collectionName: String): Unit = {
    usingCollection(db, collectionName)(col => col.drop().subscribe({
      (c: Completed) => logger.info(s"collection ${col.namespace.getCollectionName} dropped.")
    }))
  }

  /**
    * Utility func to support collection based tasks
    *
    * @param db
    * @param collectionName
    * @param fn
    * @tparam T
    * @return
    */
  def usingCollection[T](db: String, collectionName: String)(fn: MongoCollection[Document] => T): Option[T] = {
    ExpressionParser.parse(collectionName, conf) match {
      case Some(c) => usingDatabase(db) { d =>
        val mongoCollection: MongoCollection[Document] = d.getCollection(c)
        fn(mongoCollection)
      }

      case None => None
    }
  }

  /**
    * Utility func to support client based IO tasks
    *
    * @param fn Client handler function
    * @tparam T return type
    * @return
    */
  def usingMongoClient[T](fn: MongoClient => T): Option[T] = {
    Try {
      fn(mongoClient)
    } match {
      case Success(v) => Some(v)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        None
    }
  }

  def usingDatabase[T](name: String)(fn: MongoDatabase => T): Option[T] = {
    ExpressionParser.parse(name, conf) match {
      case Some(db) => usingMongoClient(_.getDatabase(db)).map(database => fn(database))
      case _ => None
    }
  }

  /**
    * Remove all the documents from the collection
    *
    * @param db
    * @param collectionName
    */
  def truncate(db: String, collectionName: String): Unit = {
    usingCollection(db, collectionName) { col =>
      col.deleteMany(BsonDocument("{}"))
        .subscribe((result: DeleteResult) =>
          logger.info(s"collection ${col.namespace.getCollectionName} truncated. ${result.getDeletedCount} rows deleted."))
    }
  }

}

/**
  * Config for MongoDB : uri format => mongodb://<host>:<port>
  *
  * @param uri
  */
class MongoDBConf(uri: String) extends Config[MongoStorage] {

  override private[jetprobe] def getStorage(sessionConf: Map[String, Any]): MongoStorage = {
    ExpressionParser.parse(uri, sessionConf) match {
      case Some(resolvedURI) => new MongoStorage(resolvedURI, sessionConf)
      case None => throw new IllegalArgumentException(s"Unable to parse MongoDB uri : ${uri}")
    }
  }

}

object MongoStorage {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._


  def jsonStrToMap0(jsonStr: String): Map[String, Any] = {
    implicit val formats = org.json4s.DefaultFormats
    parse(jsonStr).extract[Map[String, Any]]
  }
}
