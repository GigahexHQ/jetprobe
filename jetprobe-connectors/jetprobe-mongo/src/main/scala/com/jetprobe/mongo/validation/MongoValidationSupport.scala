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

  type ServerStatsAssert = ServerStats => Boolean

  def checkStats[U](expected: U, actual: ServerStats => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  ServerStatsRule[U] = ServerStatsRule(expected, actual)(fullName, line)

  def checkDBStats[U](expected: U, actual: DBStats => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  DBStatsRule[U] = DBStatsRule(expected, actual, line = line, fullName = fullName)

  def checkDatabaseList[U](expected: U, actual: DatabaseList => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  DatabaseListRule[U] = DatabaseListRule(expected, actual)

  def checkCollectionStats[U](expected: U, actual: CollectionStats => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  CollectionStatsRule[U] = CollectionStatsRule(expected, actual, line = line, fullName = fullName)

  def checkDocuments[U](query: String, expected: U, actual: SourceBsonDocuments[String] => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  DocumentsRule[U] = DocumentsRule(expected, actual, Expr(query), fullName = fullName, line = line)

  def given(dbQuery : DBQuery)(rules : DBStatsRule[_]*) : Seq[ValidationRule[MongoSink]] = {
    rules.map(r => r.copy(database = Expr(dbQuery.name)))
  }

  def given(collectionQuery: CollectionQuery)(rules : CollectionStatsRule[_]*): Seq[ValidationRule[MongoSink]] = {

    rules.map( rb => rb.copy(db = Expr(collectionQuery.database), collection = Expr(collectionQuery.collection)))
  }

  def given(docQuery: DocQuery)(rules : DocumentsRule[_]*): Seq[ValidationRule[MongoSink]] = {
    rules.map(q => q.copy(db = Expr(docQuery.database),collection = Expr(docQuery.collection),query = Expr(docQuery.query)))
  }

  //Helper methods for creating query instances. This is to avoid the need to import the objects, and take better advantage of IDE auto-completes
  def mongo(database : String) : DBQuery = new DBQuery(database)
  def mongo(database : String,collection : String) = new CollectionQuery(database,collection)
  def mongo(database : String, collection : String, query : String) = new DocQuery(database,collection,query)


  implicit object MongoValidationExecutor extends MongoValidator



}

