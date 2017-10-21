package com.jetprobe.core.reporter

import com.jetprobe.core.validations.ValidationResult

/**
  * @author Shad.
  */
trait ResultReporter {

  def report(scenario : String, className : String, results : Seq[ValidationResult]) : Unit

}


