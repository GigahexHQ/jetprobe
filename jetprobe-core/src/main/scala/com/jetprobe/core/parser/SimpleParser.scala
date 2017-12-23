package com.jetprobe.core.parser

/**
  * @author Shad.
  */

import com.typesafe.scalalogging.LazyLogging
import scala.util.parsing.combinator._
import scala.collection.mutable.ArrayBuffer


class SimpleParser extends RegexParsers {
  def word: Parser[String] =
    """[a-z]+""".r ^^ {
      _.toString
    }


  def where : Parser[String] = """"(?i)where"""".r

}

class ExpressionParser(config: Map[String, Any]) extends RegexParsers {

  //val config = Map("url" -> "http://local-host:8081", "api" -> "metadata")

  private def anyWord =
    """[A-Za-z-:./0-9_{}\[\]\?\%\=\@\(\)\'\# ",]+""".r ^^ {
      _.toString
    }

  private def aw =
    """.+?(?=\$\{)""".r

  private def start = """\$\{""".r

  private def ignoreWord =
    """\$(\w\s:',/@\[\]\=\{}\.\-")+""".r ^^ {
      _.toString
    }

  private def word =
    """[A-Za-z-.]+""".r ^^ {
      _.toString
    }

  private def end = """\}""".r

  def expr: Parser[String] = start ~ word ~ end ^^ {
    case st ~ wd ~ ed =>
      config(wd).toString
  }

  def variableParser: Parser[String] = opt(anyWord) ~ opt(ignoreWord) ~ opt(expr ~ variableParser) ^^ {
    case Some(a) ~ Some(b) ~ None => println("1"); a + b
    case Some(a) ~ None ~ Some(b ~ c) => println("2"); a + b + c
    case None ~ Some(a) ~ Some(b ~ c) => println("3"); a + b + c
    case Some(a) ~ Some(b) ~ Some(c ~ d) => println("4"); a + b + c + d
    case None ~ None ~ Some(a ~ b) => println("5"); a + b
    case _ => println("6"); ""
  }

  def vp: Parser[String] = opt(aw) ~ opt(expr) ~ opt(anyWord ~ vp) ^^ {
    case Some(a) ~ Some(b) ~ None => println("1"); a + b
    case Some(a) ~ None ~ Some(b ~ c) => println("2"); a + b + c
    case None ~ Some(a) ~ Some(b ~ c) => println("3"); a + b + c
    case Some(a) ~ Some(b) ~ Some(c ~ d) => println("4"); a + b + c + d
    case None ~ None ~ Some(a ~ b) => println("5"); a + b
    case None ~ Some(a) ~ None => println("7"); a
    case _ => println("6"); ""
  }
}

case class Expr(value: String = "")


object ExpressionParser extends LazyLogging {


  def parse(expr: String, config: Map[String, Any]): Option[String] = {

    val extractedVals: ArrayBuffer[String] = ArrayBuffer.empty

    val exprParts = expr.split("\\$")
    try {
      for (i <- exprParts.indices) {
        if (exprParts(i).nonEmpty && !exprParts(i).startsWith("{") && i != 0) {
          extractedVals += "$" + exprParts(i)
        } else if (exprParts(i).nonEmpty && exprParts(i).startsWith("{") && i != 0) {
          val endIdx = exprParts(i).indexOf("}")
          if (endIdx == -1)
            extractedVals += exprParts(i)
          else {
            extractedVals += config(exprParts(i).substring(1, endIdx)) + exprParts(i).substring(endIdx + 1)
          }
        } else if (exprParts(i).nonEmpty  && i == 0)
          extractedVals += exprParts(i)
      }
    } catch {
      case ex: Exception => logger.error(ex.getMessage)
        None
    }

    Some(extractedVals.mkString(""))

  }

  def parseAll(expressions: Seq[Expr], config: Map[String, Any]): Either[Exception, Map[String, String]] = {
    val parsed = expressions.map(expr => expr.value -> parse(expr.value, config)).toMap
    val success = parsed.values.toSet.count(_.nonEmpty)
    if (parsed.values.toSeq.count(_.nonEmpty) == expressions.toSet.size) {
      Right(parsed.mapValues(_.get))
    }
    else {
      val failedStrings = parsed.filter {
        case (k, v) => v.isEmpty
      }.keys.toArray
      val message = s"Unable to parse attributes : ${failedStrings.mkString(",")}"
      Left(new Exception(message))
    }
  }

}
