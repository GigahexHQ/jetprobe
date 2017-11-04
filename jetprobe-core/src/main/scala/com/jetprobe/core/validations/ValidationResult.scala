package com.jetprobe.core.validations

import sourcecode.{FullName, Line}

/**
  * @author Shad.
  */
case class ValidationResult(testName : String,
                             status: ValidationStatus,
                            message: String,
                            sourceCode: (FullName,Line))

object ValidationResult {

  def success(rule: ValidationRule[_]): ValidationResult = ValidationResult(rule.name,Passed, rule.onSuccess, (rule.fullName,rule.line))

  def failed(rule : ValidationRule[_], message : String) : ValidationResult = ValidationResult(rule.name,Failed,message,(rule.fullName,rule.line))

  def skipped(rule: ValidationRule[_], cause: String): ValidationResult = {
    val msg = s"Skipped Cause : $cause"
    ValidationResult(rule.name,Skipped, msg, (rule.fullName,rule.line))
  }

}

sealed trait ValidationStatus

case object Passed extends ValidationStatus
case object Failed extends ValidationStatus
case object Skipped extends ValidationStatus
case object Blocked extends ValidationStatus

