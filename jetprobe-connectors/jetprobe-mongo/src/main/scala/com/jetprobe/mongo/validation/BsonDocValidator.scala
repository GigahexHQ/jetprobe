package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.ExpressionParser
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
class BsonDocValidator(host: String, database : String, collection : String) {

  val ct = classTag[String]

  val ctStr = ct.runtimeClass

  val db : DB = new MongoClient(host).getDB(database)
  val jongo = new Jongo(db)
  val docColl = jongo.getCollection(collection)

  def validateDocs[U](rule : DocumentsRule[U],parsedQuery : String,config : Map[String,Any]) : ValidationResult = {
    val parsedRule = ExpressionParser.parse(rule.query.value,config)
    lazy val result = docColl.find(parsedQuery).as[String](classOf[String])
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
