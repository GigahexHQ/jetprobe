package com.jetprobe.mongo.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule.ActualResolver
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.mongo.models._
import com.jetprobe.mongo.sink.MongoSink
import sourcecode.{FullName, Line}


/**
  * @author Shad.
  */


case class ServerStatsRule[U <: Any](expected: U, actual: (ServerStats) => U, database: String = "test")
                             (implicit val fullName: FullName, val line: Line)
  extends ValidationRule[MongoSink] {

  override def name: String = "Server Stat validation"

  override def onSuccess: String = s"$name : had expected value as $expected"

}

case class DBStatsRule[U <: Any](expected: U, actual: (DBStats) => U, database: Expr = Expr("admin"),
                                 fullName: FullName, line: Line)
  extends ValidationRule[MongoSink] {

  override def name: String = s"Database Stats validation for $database"

  override def onSuccess: String = s"${name} : had expected value as $expected"

}

case class DatabaseListRule[U <: Any](expected: U, actual: (DatabaseList) => U, database: Expr = Expr("admin"))
                                     (implicit val fullName: FullName, val line: Line)
  extends ValidationRule[MongoSink] {

  override def name: String = s"Database Lists validation"

  override def onSuccess: String = s"$name : had expected value as $expected"

}

case class CollectionStatsRule[U <: Any](expected: U,
                                         actual: ActualResolver[CollectionStats],
                                         db: Expr = Expr(),
                                         collection: Expr = Expr(),
                                         fullName: FullName,
                                         line: Line)
  extends ValidationRule[MongoSink] {

  override def name: String = s"Stats validation for collection : $collection in database : $db"

  override def onSuccess: String = s"$name : had expected value as $expected"

}

case class DocumentsRule[U](expected: U,
                            actual: (SourceBsonDocuments[String]) => U,
                            db: Expr = Expr(),
                            collection: Expr = Expr(),
                            query: Expr = Expr(),
                            fullName: FullName,
                            line: Line)
  extends ValidationRule[MongoSink] {

  override def name: String = s"Docs validation for collection : $collection in database : $db"

  override def onSuccess: String = s"$name : had expected value as $expected"

}

/************************
  *  Models for Query
  ***********************/

class DBQuery(val name : String)
class CollectionQuery(val database : String, val collection : String)
class DocQuery(val database : String, val collection: String, val query : String)




