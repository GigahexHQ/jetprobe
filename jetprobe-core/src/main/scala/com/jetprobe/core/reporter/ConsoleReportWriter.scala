package com.jetprobe.core.reporter
import com.jetprobe.core.validations.ValidationResult

/**
  * @author Shad.
  */
class ConsoleReportWriter extends ResultReporter{

  override def report(scenario: String, className: String, results: Seq[ValidationResult]): Unit = {

    println("************************************************************")
    println(s"* Validation Report for ${scenario} *")
    println("************************************************************")
    println("Validation Summary : ")
    println(s"Class : $className , Failed : ${results.filterNot(_.isSuccess).size}, Passed : ${results.filter(_.isSuccess).size}")
    results.filterNot(_.isSuccess).foreach{ res =>
      println(s"${Console.RED}   ${res.msgOnFailure.get}")
    }
    println(Console.WHITE)


  }
}
