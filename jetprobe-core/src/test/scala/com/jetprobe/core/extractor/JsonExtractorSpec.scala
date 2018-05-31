package com.jetprobe.core.extractor

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
  * @author Shad.
  */
class JsonExtractorSpec extends FlatSpec with Matchers{

  behavior of "JsonExtractor"

  it should "build the json path" in {

    val jsonPath = JsonPathExtractor.jsonPath("$.[0].name",saveAs = "username")
    jsonPath.path should equal("$.[0].name")


    val jsonString = Source.fromFile(getClass.getResource("/jsons/post.json").getFile).getLines().mkString("")
    val map = jsonPath.extractFrom[String](jsonString)

    map("username") should equal("Cruz Gross")

    val invalidJsonPath = JsonPathExtractor.jsonPath("$.[0].uid",saveAs = "id")
    val emptymap = invalidJsonPath.extractFrom[String](jsonString)

    emptymap should equal(Map.empty)
  }


}
