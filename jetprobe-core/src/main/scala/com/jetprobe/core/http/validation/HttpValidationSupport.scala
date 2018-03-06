package com.jetprobe.core.http.validation

import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.validations.ValidationRule

/**
  * @author Shad.
  */
trait HttpValidationSupport {

  def given[T](jsonQuery : String)(fnRule : T => Any) : ValidationRule[HttpRequestBuilder] = {
    JsonResponseRule[T](query = jsonQuery,fnActual = fnRule)
  }

  //implicit object HttpValidationExecutor extends HttpValidator
}


trait HttpRequestConditions {

  def havingJsonQuery[T](jq : String,cls : Class[T]) : JsonQuery = JsonQuery(jq)



}

case class JsonQuery(jq : String)