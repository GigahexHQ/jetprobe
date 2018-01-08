package com.jetprobe.core.validations

import com.jetprobe.core.storage.{DataSource, Storage}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
trait ValidationRule[D <: DataSource] {

  def validate(config : Map[String,Any], storage : D) : ValidationResult

  def name: String

}

class PropertyBasedValidtation[D,S <: Storage](propertyVal : D, fn : D => Any,line: sourcecode.Line, fullName: sourcecode.FullName) extends ValidationRule[S] {

  override def name: String = s"Test at ${fullName.value}:${line.value}"

  override def validate(config: Map[String, Any], storage: S): ValidationResult = {
    val tryRes = Try(fn(propertyVal))
    tryRes match {
      case Success(_) => ValidationResult.success(this)
      case Failure(ex) => ValidationResult.failed(this,ex.getMessage)
    }
  }

}

class MayBePropertyValidation[D,S <: Storage](propertyVal : Option[D], fn : D => Any,line: sourcecode.Line, fullName: sourcecode.FullName) extends ValidationRule[S] {

  override def name: String = s"Test at ${fullName.value}:${line.value}"

  override def validate(config: Map[String, Any], storage: S): ValidationResult = {
    propertyVal match {
      case Some(value) =>
        val tryRes = Try(fn(value))
        tryRes match {
          case Success(_) => ValidationResult.success(this)
          case Failure(ex) => ValidationResult.failed(this,ex.getMessage)
        }

      case None =>
        ValidationResult.failed(this,"Value not found")
    }

  }

}

object ValidationRule {

  type ActualResolver[T <: DataSource] = T => Any

}

