package com.jetprobe.core.parser

/**
  * @author Shad.
  */

import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable.ArrayBuffer

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
