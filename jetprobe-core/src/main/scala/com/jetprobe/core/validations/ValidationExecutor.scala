package com.jetprobe.core.validations

import com.jetprobe.core.sink.{DataSink, DataSource}

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author Shad.
  */
trait ValidationExecutor[D <: DataSource] {

  def execute(rules: Seq[ValidationRule[D]], sink: D, config : Map[String,Any]): Seq[ValidationResult]


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

  def validateGivenRule[T <: DataSource,R <: ValidationRule[_]](meta: Either[Exception, T], rule: R)
                               (validator: (T, R) => ValidationResult)
  : ValidationResult = {

    meta match {
      case Left(error) => ValidationResult.skipped(rule, error.getMessage)
      case Right(fetchedMeta) => validator(fetchedMeta, rule)

    }
  }

}
