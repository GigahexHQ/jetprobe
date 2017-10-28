package com.jetprobe.core.parser

/**
  * @author Shad.
  */

import scala.util.parsing.combinator._

class SimpleParser extends RegexParsers {
  def word: Parser[String] =
    """[a-z]+""".r ^^ {
      _.toString
    }
}

class ExpressionParser(config: Map[String, Any]) extends RegexParsers {

  //val config = Map("url" -> "http://local-host:8081", "api" -> "metadata")

  private def anyWord = """[A-Za-z-:./0-9_{} ",]+""".r ^^ {_.toString}

  private def start = """\$\{""".r

  private def word =
    """[A-Za-z-.]+""".r ^^ {
      _.toString
    }

  private def end = """\}""".r

  def expr: Parser[String] = start ~ word ~ end ^^ {
    case st ~ wd ~ ed =>
      config(wd).toString
  }

  def variableParser: Parser[String] = opt(anyWord) ~ opt(expr ~ variableParser) ^^ {
    case Some(a) ~ None => a
    case Some(a) ~ Some(b ~ c) => a + b + c
    case None ~ Some(b ~ c) => b + c
    case _ => ""
  }
}

case class Expr(value : String = "")


object ExpressionParser {

  def parse(parsable: String, config: Map[String, Any]): Option[String] = {
    val parser = new ExpressionParser(config)
    val result = parser.parseAll(parser.variableParser, parsable)
    result match {
      case x if x.successful => Some(x.get)
      case _ => None
    }
  }

  def parseAll(expressions: Seq[Expr], config: Map[String, Any]): Either[Exception,Map[String, String]] = {
    val parsed = expressions.map(expr => expr.value -> parse(expr.value, config)).toMap
    val success = parsed.values.toSet.count(_.nonEmpty)
    println(s"Parsed succcess : ${success}. expected : ${expressions.toSet.size}")
    if(parsed.values.toSeq.count(_.nonEmpty) == expressions.toSet.size){
      Right(parsed.mapValues(_.get))
    }
    else{
      val failedStrings = parsed.filter{
        case (k,v) => v.isEmpty
      }.keys.toArray
      println(s"Failed string : ${failedStrings.toList}")
      val message = s"Unable to parse attributes : ${failedStrings.mkString(",")}"
      Left(new Exception(message))
    }
  }

}

object TestSimpleParser {

  def main(args: Array[String]) = {
    //val result = parse(word,"Shad Amez")
    val conf = Map("app.url" -> "https://jetprobe.com/", "app-host" -> "jsonplaceholder.typicode.com")
    val json = "https://${app-host}/posts/1"
    val json2 = "https://reqres.in/api/users"
    val json3 = "${app-host}"

    println(ExpressionParser.parse(json3,conf))
  }
}