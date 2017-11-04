package com.jetprobe.core.reporter

import com.jetprobe.core.validations.{ValidationResult, ValidationStatus}

/**
  * @author Shad.
  */
trait ResultReporter {

  def report(scenario : String, className : String, results : Seq[ValidationResult]) : Unit = ???

  def write(report : Seq[ValidationReport]) : Unit = ???

}

case class ValidationReport(suite : String,
                            failedCount : Int,
                            successCount : Int,
                            skippedCount : Int,
                            timeTaken : Double,
                            finalSatus : ValidationStatus,
                            detailReport : Seq[ValidationResult])

object ValidationReport {


}


