package com.jetprobe.core.validations

import com.jetprobe.core.sink.DataSource
import com.jetprobe.core.validations.ValidationRule.ActualResolver

/**
  * @author Shad.
  */
trait ValidationRule[D] {

  val expected: Any


  val actual: ActualResolver[_]

  def name: String

  def line: sourcecode.Line

  def fullName: sourcecode.FullName

  def onSuccess: String = s"${name} passed"

  def onFailure[U <: Any](actual: U, expected: U, given: String): String = s"Validation failed at ${fullName.value}:${line.value}. Given $given," +
    s" Expected = $expected Found = $actual"


}

object ValidationRule {

  type ActualResolver[T <: DataSource] = T => Any

}

