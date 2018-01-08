package com.jetprobe.core.validations

import sourcecode.{FullName, Line}

/**
  * @author Shad.
  */
case class ValidationResult(testName : String,
                             status: ValidationStatus,
                            message: String = "Validation Passed")

object ValidationResult {

  def getFailedMessage(expected : Any, actual : Any) : String = {
    s"Expected = $expected, Actual = $actual"
  }
  def success() = ValidationResult("Test",Passed, "Passed")
  def failed(message : String, name : FullName,line : Line) = ValidationResult("Test",Failed, message)

  def failed(message : String) = ValidationResult("Test",Failed, message)

  def success(rule: ValidationRule[_]): ValidationResult = ValidationResult(rule.name,Passed)

  def failed(rule : ValidationRule[_], cause : String) : ValidationResult = {
    val message = s"${rule.name} Failed. Cause : ${cause}"
    ValidationResult(rule.name,Failed,message)
  }

  def skipped(rule: ValidationRule[_], cause: String): ValidationResult = {
    val msg = s"Validation was skipped. Cause : $cause"
    ValidationResult(rule.name,Skipped, msg)
  }

}

sealed trait ValidationStatus

case object Passed extends ValidationStatus
case object Failed extends ValidationStatus
case object Skipped extends ValidationStatus
case object Blocked extends ValidationStatus

