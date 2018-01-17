package com.jetprobe.core.validations

import com.jetprobe.core.storage.DataSource
import com.jetprobe.core.validations.ValidationExecutor.Parsed


import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * @author Shad.
  */
trait ValidationExecutor[D <: DataSource]  {

  import ValidationExecutor.Parsed

  type Extractor[T] = T => Any

  type ValidationFn[T <: Any] = Seq[T] => Any


  def validateResponse[T](parsed: Parsed[T], extractor: Extractor[T]): ValidationResult = {

    val resultFuture = parsed.map {
      case Left(ex) => ValidationResult.failed(ex.getMessage)
      case Right(param) =>
        val result = Try {
          extractor.apply(param)
        }
        result match {
          case Success(_) => ValidationResult.success()
          case Failure(exception) =>
            ValidationResult.failed(exception.getMessage)
        }
    }

    Await.result(resultFuture, 50.seconds)

  }

}

trait RuleValidator {

  type Extractor[T] = T => Any

  def validateResponse[T](value : T,extractor: Extractor[T]) : ValidationResult = {
    val tryResult = Try { extractor(value)  }

    tryResult match {
      case Success(_) => ValidationResult.success()
      case Failure(ex) => ValidationResult.failed(ex.getMessage)
    }

  }

  def validateResponse[T](parsed: Parsed[T], extractor: Extractor[T],rule : ValidationRule[_]): ValidationResult = {

    val resultFuture = parsed.map {
      case Left(ex) => ValidationResult.failed(rule,ex.getMessage)
      case Right(param) =>
        val result = Try {
          extractor.apply(param)
        }

        result match {
          case Success(_) => ValidationResult.success()
          case Failure(exception) =>
            ValidationResult.failed(exception.getMessage)
        }
    }

    Await.result(resultFuture, 10.seconds)

  }

}

object ValidationExecutor {

  type Parsed[T] = Future[Either[Throwable, T]]

  def validate[T <: DataSource](meta: Either[Exception, T], rule: ValidationRule[_])
                               (validator: (T, ValidationRule[_]) => ValidationResult)
  : ValidationResult = {

    meta match {
      case Left(error) => ValidationResult.skipped(rule, error.getMessage)
      case Right(fetchedMeta) => validator(fetchedMeta, rule)

    }
  }


}
