package com.jetprobe.core.reporter

import java.util.Date

import com.jetprobe.core.validations.{ValidationResult, ValidationStatus}

/**
  * @author Shad.
  */
trait ResultReporter[O] {

  def report(scenario : String, className : String, results : Seq[ValidationResult]) : Unit = ???

  def write(reports : Seq[ValidationReport]) : O = ???

}

case class ValidationReport(suite : String,
                            className : String,
                            failedCount : Int,
                            successCount : Int,
                            skippedCount : Int,
                            finalSatus : ValidationStatus,
                            detailReport : Seq[ValidationResult])





