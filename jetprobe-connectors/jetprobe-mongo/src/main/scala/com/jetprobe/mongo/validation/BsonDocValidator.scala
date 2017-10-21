package com.jetprobe.mongo.validation

import com.jetprobe.core.validations.ValidationResult
import com.jetprobe.mongo.models.SourceBsonDocuments
import com.jetprobe.mongo.sink.MongoSink
import com.mongodb.{DB, MongoClient}
import org.jongo.Jongo
import com.jetprobe.core.validations.ValidationHelper.getFailureMessage
import scala.reflect._
import scala.collection.JavaConverters._
/**
  * @author Shad.
  */
class BsonDocValidator(sink : MongoSink, database : String, collection : String) {

  val ct = classTag[String]

  val ctStr = ct.runtimeClass

  println(s"mongo host : ${sink.host.substring(10)}")
  val db : DB = new MongoClient(sink.host.substring(10)).getDB(database)
  val jongo = new Jongo(db)
  val docColl = jongo.getCollection(collection)

  def validateDocs[U](rule : DocumentsRule[U]) : ValidationResult = {
    lazy val result = docColl.find(rule.query).as[String](classOf[String])
    val docs = SourceBsonDocuments(result.count(),result.iterator().asScala)
    val actualResult = rule.actual(docs)
    actualResult == rule.expected match {
      case true => ValidationResult(true, Some(rule.onSuccess), None)
      case false =>
        val failureMessage = getFailureMessage(rule.name, actualResult,
          rule.expected, rule.fullName.value, rule.line.value)
        ValidationResult(false, None, Some(failureMessage))
    }
  }



}
