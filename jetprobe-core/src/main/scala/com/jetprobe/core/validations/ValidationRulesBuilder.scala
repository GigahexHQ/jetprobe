package com.jetprobe.core.validations

import com.jetprobe.core.sink.DataSource

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
trait ValidationRulesBuilder[D <: DataSource] {

  def build : ArrayBuffer[ValidationRule[D]]


}

trait ValidationRuleBuilder[T,U,D <: DataSource] {

  def assert(expected : U, calculate : T => U) : ValidationRule[D]
}
