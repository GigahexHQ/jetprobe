package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.ValidationBuilder
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.validations.{ValidationExecutor, ValidationRule, ValidationRulesBuilder}

/**
  * @author Shad.
  */
trait Validations[B] extends Execs[B]{

  def validate[D <: DataSource](sink : D)(f : D => Seq[ValidationRule[D]])(implicit executor : ValidationExecutor[D]) : B = {
    val rules = f(sink)
    //println(rules.build.size)
    exec(new ValidationBuilder[D](sink,rules)(executor))
  }
}
