package com.jetprobe.core.reporter
import com.jetprobe.core.validations.{Passed, ValidationResult}

/**
  * @author Shad.
  */
class ConsoleReportWriter extends ResultReporter{

  override def report(scenario: String, className: String, results: Seq[ValidationResult]): Unit = {

    println("************************************************************")
    println(s"* Validation Report for ${scenario} *")
    println("************************************************************")
    println("Validation Summary : ")
    println(s"Class : $className , Failed : ${results.filterNot(_.status.equals(Passed)).size}, Passed : ${results.filter(_.status.equals(Passed)).size}")
    results.filterNot(_.status.equals(Passed)).foreach{ res =>
      println(s"${Console.RED} Test at ${res.sourceCode._1.value}:${res.sourceCode._2.value} ${res.status.toString.toLowerCase}. Cause : ${res.message}")
    }
    println(Console.WHITE)


  }
}
