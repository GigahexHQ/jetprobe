package com.jetprobe.core.validations

import com.jetprobe.core.sink.DataSource
import com.jetprobe.core.validations.ValidationRule.ActualResolver

/**
  * @author Shad.
  */
trait ValidationRule[D] {

  def validate(config : Map[String,Any]) : ValidationResult = ???

  def name: String

}

object ValidationRule {

  type ActualResolver[T <: DataSource] = T => Any

}

