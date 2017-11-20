package com.jetprobe.core.reporter
import com.jetprobe.core.validations.{Failed, Passed, Skipped, ValidationResult}

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
    results.filter(_.status.equals(Failed)).foreach{ res =>

      println(s"${Console.WHITE} ${res.message}")
    }
    println(Console.WHITE)
    results.filter(_.status.equals(Skipped)).foreach{ res =>
      println(s"${Console.WHITE} ${res.message}")
    }
  }

  override def write(reports: Seq[ValidationReport]): Unit = {

    reports.foreach(rep => {
      report(rep.suite,rep.className,rep.detailReport)
    })

  }
}
