package com.jetprobe.core.parser

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
  * @author Shad.
  */
class ExpressionParserSpec extends FlatSpec with Matchers{

  val config : Map[String,Any] = Map("hello.name" -> "Shad","hola.location" -> "Spain",
    "mdm.tenantId" -> 100,"mdm.username" -> "vijay",
    "app.host" -> "server",
  "mdm.entity" -> "customer")
  behavior of "expression parsers"

  it should "parse single variables" in {
    val json = """${hello.name}"""
    val extractedVal = ExpressionParser.parse(json,config)
    extractedVal should equal(Some("Shad"))

  }

  it should "parse mutliple variables" in {
    val json = """${hello.name} ${hola.location}"""
    val extractedVal = ExpressionParser.parse(json,config)
    extractedVal should equal(Some("Shad Spain"))

  }

  it should "escape dummy $ variables" in {
    val json = """$ref ${hello.name} ${hola.location}"""
    val extractedVal = ExpressionParser.parse(json,config)
    extractedVal should equal(Some("$ref Shad Spain"))

  }

  it should "work with special characters" in {
    val json = "https://${app.host}/posts/1${mdm.tenantId}.${mdm.username}"
    val extractedVal = ExpressionParser.parse(json,config)
    extractedVal should equal(Some("https://server/posts/1100.vijay"))

  }

  it should "work with complex characters" in {
    val json = """{"A$ref": "//@businessEntity[guid='${mdm.entity}']/@field[name='address']/@field[name='${mdm.tenantId}']"}{"name" : "Ajay"}]"""
    val extractedVal = ExpressionParser.parse(json,config)
    extractedVal should equal(Some("""{"A$ref": "//@businessEntity[guid='customer']/@field[name='address']/@field[name='100']"}{"name" : "Ajay"}]"""))

  }

  it should "work with loooong string" in {
    val json = Source.fromFile("C:\\Users\\samez\\Documents\\match-service\\MDMN-9162\\fuzzy-model.json").mkString("").split('\n').map(_.trim.filter(_ >= ' ')).mkString
    val extractedVal = ExpressionParser.parse(json,config)
    assert(extractedVal.nonEmpty)
    println(extractedVal.get)

  }

}
