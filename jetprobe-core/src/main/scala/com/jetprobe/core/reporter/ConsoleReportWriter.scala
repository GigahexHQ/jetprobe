package com.jetprobe.core.reporter

import com.jetprobe.core.validations.{Failed, Passed, Skipped, ValidationResult}

/**
  * @author Shad.
  */
class ConsoleReportWriter extends ResultReporter {

  override def report(scenario: String, className: String, results: Seq[ValidationResult]): Unit = {

    println("************************************************************")
    println(s"* Validation Report for ${scenario} *")
    println("************************************************************")
    if(results.size == results.count(_.status == Passed)){
      println("Validation Summary : All validations passed \uD83D\uDE0A")
    }else{
      println("Validation Summary : ")
    }

    println(s"Class : $className , Failed : ${results.count(_.status == Failed)}," +
      s" Passed : ${results.filter(_.status.equals(Passed)).size}" +
      s" Skipped : ${results.count(_.status == Skipped)}")
    results.filter(_.status.equals(Failed)).foreach { res =>

      println(s"${res.message} ")
    }
    results.filter(_.status.equals(Skipped)).foreach { res =>
      println(s" ${res.message}")
    }
  }

  override def write(reports: Seq[ValidationReport]): Unit = {

    reports.foreach(rep => {
      report(rep.suite, rep.className, rep.detailReport)
    })

  }
}
