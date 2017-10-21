package com.jetprobe.core.validations

import com.jetprobe.core.sink.{DataSink, DataSource}

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author Shad.
  */
trait ValidationExecutor[D <: DataSource] {

  def execute(rulesBuilder: Seq[ValidationRule[D]], sink: D): Seq[ValidationResult]


}

object ValidationExecutor {

  def validate[T <: DataSource](meta: Either[Exception, T], rule: ValidationRule[_])
                               (validator: (T, ValidationRule[_]) => ValidationResult)
  : ValidationResult = {

    meta match {
      case Left(error) => ValidationResult.skipped(rule, error.getMessage)
      case Right(fetchedMeta) => validator(fetchedMeta, rule)

    }
  }

}
