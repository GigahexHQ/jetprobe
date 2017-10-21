package com.jetprobe.core.validations

/**
  * @author Shad.
  */
case class ValidationResult(isSuccess: Boolean,
                            msgOnSuccess: Option[String],
                            msgOnFailure: Option[String])

object ValidationResult {

  def success(rule: ValidationRule[_]): ValidationResult = ValidationResult(true, Some(rule.onSuccess), None)

  def skipped(rule: ValidationRule[_], cause: String): ValidationResult = {
    val msg = s"${rule.name} was skipped at ${rule.fullName.value}:${rule.line.value}. Expected = ${rule.expected}. Skipped Cause : $cause"
    ValidationResult(false, None, Some(msg))
  }

}
