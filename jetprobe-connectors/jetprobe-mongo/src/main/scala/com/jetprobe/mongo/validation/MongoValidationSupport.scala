package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.mongo.models._
import com.jetprobe.mongo.sink.MongoSink

/**
  * @author Shad.
  */

trait MongoValidationSupport {

  //implicit def mongoSinkValidator(sink : MongoSink) : MongoValidationRulesBuilder = new MongoValidationRulesBuilder(sink)
  import io.circe.generic.auto._

  type ServerStatsAssert = ServerStats => Boolean

  def checkCollectionStats[U](expected: U, actual: CollectionStats => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  CollectionStatsRule[U] = CollectionStatsRule(expected, actual)

  def checkDocuments[U](expected: U, actual: SourceBsonDocuments[Any] => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  DocumentsRule[U] = DocumentsRule(expected, actual)

  def given(dbQuery : DBQuery)(ruleFn : DBStats => Any) : ValidationRule[MongoSink] = {
    DatabaseStatsRule(ruleFn,Expr(dbQuery.name))
  }

  //Validation DSL for MongoDB server
  def given(server : ServerQuery)(ruleFn : ServerStats => Any) : ValidationRule[MongoSink] = {
    MongoServerStatsRule(ruleFn)
  }

//
  def given(docQuery: DocQuery)(rules : DocumentsRule[_]*): Seq[ValidationRule[MongoSink]] = {
    rules.map(q => q.copy(db = Expr(docQuery.database),collection = Expr(docQuery.collection),query = Expr(docQuery.query)))
  }

  def given[T](query : MongoSQL[T])(ruleFn : Seq[T] => Any) : ValidationRule[MongoSink] = MongoQueryRule[T](ruleFn,Expr(query.sql),query.decoder,None)

  /*implicit object MongoValidationExecutor extends MongoValidator*/
  implicit object MongoValidationExecutor extends MongoValidator

}

