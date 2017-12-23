package com.jetprobe.core.validations

import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.validations.ValidationExecutor.Parsed
import io.circe.Error

import scala.concurrent.{Await, ExecutionContext, Future}
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

  def execute(rules: Seq[ValidationRule[D]], sink: D, config : Map[String,Any]): Seq[ValidationResult]

  def validateResponse[T](parsed: Parsed[T], extractor: Extractor[T]): ValidationResult = {

    val resultFuture = parsed.map {
      case Left(ex) => ValidationResult.failed(ex.getMessage)
      case Right(param) => //handleAssertion[DBStats](databaseSt,extractor,className,line)
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

  def validateCollectionResponse[T](parsed: Parsed[Seq[T]], extractor: Seq[T] => Any): ValidationResult = {

    val resultFuture = parsed.map {
      case Left(ex) => ValidationResult.failed(ex.getMessage)
      case Right(param) => //handleAssertion[DBStats](databaseSt,extractor,className,line)
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

  def validateResponse[T](parsed: Parsed[T], extractor: Extractor[T]): ValidationResult = {

    val resultFuture = parsed.map {
      case Left(ex) => ValidationResult.failed(ex.getMessage)
      case Right(param) => //handleAssertion[DBStats](databaseSt,extractor,className,line)
        val result = Try {
          extractor.apply(param)
        }
        result match {
          case Success(_) => ValidationResult.success()
          case Failure(exception) =>
            ValidationResult.failed(exception.getMessage)
        }
    }

    Await.result(resultFuture, 300.seconds)

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

  def validateGivenRule[T <: DataSource,R <: ValidationRule[_]](meta: Either[Exception, T], rule: R)
                               (validator: (T, R) => ValidationResult)
  : ValidationResult = {

    meta match {
      case Left(error) => ValidationResult.skipped(rule, error.getMessage)
      case Right(fetchedMeta) => validator(fetchedMeta, rule)

    }
  }

}
