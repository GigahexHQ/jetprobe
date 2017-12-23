package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.ValidationBuilder
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.validations._

/**
  * @author Shad.
  */
trait Validations[B] extends Execs[B]{

  /*def validate[D <: DataSource](sink : D)(rules :Seq[ValidationRule[D]])
                               (implicit executor : ValidationExecutor[D]) : B = {

    exec(new ValidationBuilder[D](sink,rules)(executor))
  }*/


  def validate[D <: DataSource](sink : D)(rules :ValidationRule[D]*)
                               (implicit executor : ValidationExecutor[D]) : B = {

    exec(new ValidationBuilder[D](sink,rules)(executor))
  }



}
