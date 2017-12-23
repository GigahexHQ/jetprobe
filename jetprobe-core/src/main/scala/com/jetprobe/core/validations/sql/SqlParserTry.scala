package com.jetprobe.core.validations.sql



import scala.collection.JavaConverters._
import scala.util.parsing.combinator._
/**
  * @author Shad.
  */
object SqlParserTry extends App{


}

import scala.util.parsing.combinator._


class SqlTokenParser extends RegexParsers{

  def ws : Parser[String] = """\s+""".r

  def query : Parser[Any] = selectClouse ~ fromClouse

  def ident : Parser[String] = " ".r

  def stringLiteral : Parser[String] = "\\w+".r

  def subquerys : Parser[Any] = repsep(subquery,",")

  def subquery : Parser[Any] = ws ~ "("~ ws  ~ query ~ ws ~ ")"~ ws ~ opt(ident)

  def column : Parser[Any] =  "*" | ident | stringLiteral ~ opt(ws ~"as"~ ws ~ ident)

  def columns : Parser[Any] = repsep(column,",")

  def selectClouse : Parser[Any] = "select" ~  repsep( columns | subquerys ,",")

  def tableNames : Parser[Any] = repsep(tableName,",")

  def tableName : Parser[Any] = ident | "dual"  ~ opt(ws ~ "as"~ ws ~ ident)

  def fromClouse : Parser[Any] = "from" ~ repsep(tableNames | subquerys,",")

}

import java.io.{File,FileReader}
object ST extends SqlTokenParser {


  //val parseA = P( "a" )

  def main(args: Array[String]) {


  }
}
