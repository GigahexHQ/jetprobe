package com.jetprobe.mongo.validation

import java.util

import com.jetprobe.core.extractor.JsonPathExtractor.extractJsonVal
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

  val db : DB = new MongoClient(host.substring(10)).getDB(database)
  val jongo = new Jongo(db)
  val docColl = jongo.getCollection(collection)

  def validateDocs[U](rule : DocumentsRule[U],parsedQuery : String,config : Map[String,Any]) : ValidationResult = {
    val parsedRule = ExpressionParser.parse(rule.query.value,config)
    val result = docColl.find(parsedQuery).as[Any](classOf[Any])
    val docs = SourceBsonDocuments(result.count(),result.iterator().asScala)
    val actualResult = rule.actual(docs)
    actualResult == rule.expected match {
      case true => ValidationResult.success(rule)
      case false =>
        val failureMessage = getFailureMessage(rule.name, actualResult, rule.expected)
        ValidationResult.failed(rule,failureMessage)
    }
  }


}

object BsonDocValidator extends MongoValidationSupport with App{

  val docValidator = new BsonDocValidator("mongodb://192.168.37.128","some","employee3")
  val rule = checkDocuments(true,_.documents.forall(record => extractJsonVal("cat",record.asInstanceOf[util.LinkedHashMap[String,Any]]).equals("Prog")))
  docValidator.validateDocs(rule,"{_id : 2}",Map.empty)

}
