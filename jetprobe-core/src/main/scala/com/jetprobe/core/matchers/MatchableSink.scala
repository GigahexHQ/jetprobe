package com.jetprobe.core.matchers

import com.jetprobe.core.sink.{DataSink, MongoSink}
import org.scalatest._
import matchers.{MatchResult, Matcher}
/**
  * @author Shad.
  */
trait MatchableSink[T <: DataSink]

trait MongoMatchables extends MatchableSink[MongoSink] {

  class HasDatabaseMatcher(expectedDatabase : String) extends Matcher[MongoSink] {
    override def apply(left: MongoSink): MatchResult = {
      MatchResult(
        left.db.equals(expectedDatabase),
        s"""database $left.db did not equal "$expectedDatabase""",
        s"""database $left.db equals "$expectedDatabase"""
      )
    }
  }

  def hasDatabase(expectedDatabase : String) : Matcher[MongoSink] = new HasDatabaseMatcher(expectedDatabase)

}

object MongoMatchables extends MongoMatchables



trait CustomMatchers {

  class FileEndsWithExtensionMatcher(expectedExtension: String) extends Matcher[java.io.File] {

    def apply(left: java.io.File) : MatchResult = {
      val name = left.getName
      MatchResult(
        name.endsWith(expectedExtension),
        s"""File $name did not end with extension "$expectedExtension"""",
        s"""File $name ended with extension "$expectedExtension""""
      )
    }
  }

}