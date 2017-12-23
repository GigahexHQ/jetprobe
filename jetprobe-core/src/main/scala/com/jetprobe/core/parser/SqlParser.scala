package com.jetprobe.core.parser

import fastparse.core.Parsed.{Failure, Success}

/**
  * @author Shad.
  */
/*object SqlParser extends App{

  import fastparse.all._


  case class ParsedSQL(fields : Array[String] = Array.empty ,target : String = "")

  val p = ParsedSQL()

  val selectKeyword : P[ParsedSQL] = P(IgnoreCase("select").rep ~ " " ).!.map(x => p)

  val selectedFieds : P[ParsedSQL] = P(CharIn('a'to'z').rep(sep=",")).!.map {str =>
    println(s"parsed fields : ${str}")
    p.copy(fields = str.split(","))
  }

  val parseSQL  = P(Start ~ selectKeyword ~ selectedFieds ~ End)

  val result = selectedFieds.parse("hola,adb")

  result match {
    case Success(v,idx) => println("Successfully parsed")
      v.fields.foreach(println)
    case Failure(l,i,t) => println("parser failed")
  }
}*/

object SqlChecker {

  def main(args: Array[String]): Unit = {

    val sampleSql = "select e.name,abc.cd.salary as sal ,age from employee where sal >= 1000000000"


    val parser = new SQLParser
    val r = parser.parse(sampleSql).map(parser.extract(_))

    println(r.get)

  }

}
