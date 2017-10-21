package com.jetprobe.core.parser

/**
  * @author Shad.
  */
import scala.util.parsing.combinator._

class SimpleParser extends RegexParsers{
  def word: Parser[String]    = """[a-z]+""".r ^^ { _.toString }
}

class ExpressionParser extends RegexParsers {

  val config = Map("url" -> "http://local-host:8081", "api" -> "metadata")

  def anyWord = """[A-Za-z-:/0-9_{} ",]+""".r ^^ { _.toString}
  def start = """\$\{""".r
  def word = """[a-z]+""".r ^^ { _.toString }
  def end = """\}""".r
  def expr: Parser[String] = start ~ word ~ end  ^^ {
    case  st ~ wd ~ ed  => config.get(wd).get
  }

  def urlExpr : Parser[String] = opt(anyWord) ~  opt(expr ~ urlExpr) ^^ {
    case Some(a) ~ None => a
    case Some(a) ~ Some(b ~ c) => a + b + c
    case None ~ Some(b ~ c) => b + c
    case _ => ""
  }
}

object TestSimpleParser extends ExpressionParser {

  def main(args: Array[String]) = {
    //val result = parse(word,"Shad Amez")

    val json = "{\"title\": \"Person\", \"uri\" : \"${url}/${api}\" }"
    println(parseAll(urlExpr, json))
  }
}