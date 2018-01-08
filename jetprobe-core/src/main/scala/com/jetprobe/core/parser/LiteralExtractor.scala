package com.jetprobe.core.parser

/**
  * @author Shad.
  */
object LiteralExtractor {

  case class ParseOp[T](op: String => T)

  implicit val popFloat = ParseOp[Float](_.toFloat)
  implicit val popInt = ParseOp[Int](_.toInt)
  implicit val popLong = ParseOp[Long](_.toLong)

  // etc.
  def parse[T: ParseOp](s: String) = try {
    Some(implicitly[ParseOp[T]].op(s))
  }
  catch {
    case _ => None
  }


}
