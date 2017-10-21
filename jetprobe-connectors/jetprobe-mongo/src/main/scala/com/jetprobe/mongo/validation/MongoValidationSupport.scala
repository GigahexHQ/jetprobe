package com.jetprobe.mongo.validation

import com.jetprobe.mongo.models._
import com.jetprobe.mongo.sink.MongoSink

/**
  * @author Shad.
  */

object MongoValidationSupport {

  implicit def mongoSinkValidator(sink : MongoSink) : MongoValidationRulesBuilder = new MongoValidationRulesBuilder(sink)

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
  DocumentsRule[U] = DocumentsRule(expected, actual, query, fullName = fullName, line = line)

  implicit object MongoValidationExecutor extends MongoValidator



}

