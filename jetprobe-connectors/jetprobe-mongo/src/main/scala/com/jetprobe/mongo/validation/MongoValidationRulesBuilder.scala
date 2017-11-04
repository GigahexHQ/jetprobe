package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.{ValidationRule, ValidationRulesBuilder}
import com.jetprobe.mongo.sink.MongoSink

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class MongoValidationRulesBuilder(mongoSink: MongoSink) extends ValidationRulesBuilder[MongoSink] {

  val mongoValidationRules: ArrayBuffer[ValidationRule[MongoSink]] = ArrayBuffer.empty

  import MongoValidationRulesBuilder._

  val mongoContext: mutable.Map[String, Any] = mutable.Map.empty

  def forServer(ruleBuilders: ServerStatsRule[_]*)(implicit d: DummyImplicit): Seq[ValidationRule[MongoSink]] = {
    ruleBuilders.toSeq
    //mongoValidationRules.++=(ruleBuilders)
    //this
  }

  def forDatabase(db: String, ruleBuilders: DBStatsRule[_]*): Seq[ValidationRule[MongoSink]] = {
    ruleBuilders.map {
      case rb: DBStatsRule[_] => rb.copy(database = Expr(db))
    }

  }

  def forDatabaseAndCollection(database: String, collection: String, ruleBuilders: CollectionStatsRule[_]*): Seq[ValidationRule[MongoSink]] = {

    ruleBuilders.map {
      case rb: CollectionStatsRule[_] => rb.copy(db = Expr(database), collection = Expr(collection))
    }
    //addAll(ruleDBAndColls)
  }

  def forDatabaseAndCollection(database: String, collection: String, ruleBuilders: DocumentsRule[_]*)
                              (implicit d: DummyImplicit): Seq[ValidationRule[MongoSink]] = {

    ruleBuilders.map {
      case rb: DocumentsRule[_] => rb.copy(db = Expr(database), collection = Expr(collection))
    }

  }

  def forServer(ruleBuilders: DatabaseListRule[_]*): Seq[ValidationRule[MongoSink]] = {
    ruleBuilders.toSeq
  }

  private def chain(ruleBuilders: ValidationRule[MongoSink]*): ValidationRulesBuilder[MongoSink] = {
    mongoValidationRules.++=(ruleBuilders)
    this
  }

  override def build: ArrayBuffer[ValidationRule[MongoSink]] = rules


}

object MongoValidationRulesBuilder {

  val rules: ArrayBuffer[ValidationRule[MongoSink]] = ArrayBuffer.empty

  private[mongo] def addAll(ruleBuilders: Seq[ValidationRule[MongoSink]]): Seq[ValidationRule[MongoSink]] = {
    rules.++=(ruleBuilders)
  }
}
