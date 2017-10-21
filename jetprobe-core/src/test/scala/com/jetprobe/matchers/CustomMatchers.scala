package com.jetprobe.matchers

import org.scalatest._
import matchers._
/**
  * @author Shad.
  */
trait CustomMatchers {

  class FileEndsWithExtensionMatcher(expectedExtension: String) extends Matcher[java.io.File] {

    def apply(left: java.io.File) = {
      val name = left.getName
      MatchResult(
        name.endsWith(expectedExtension),
        s"""File $name did not end with extension "$expectedExtension"""",
        s"""File $name ended with extension "$expectedExtension""""
      )
    }
  }

}
