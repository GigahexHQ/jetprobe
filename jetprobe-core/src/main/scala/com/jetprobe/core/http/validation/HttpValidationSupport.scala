package com.jetprobe.core.http.validation

import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.validations.ValidationRule

/**
  * @author Shad.
  */
trait HttpValidationSupport {
/*
  def checkHttpResponse[U](expected: U, actual: FetchedResponse => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  HttpResponseValidationRule[U] = HttpResponseValidationRule(expected, actual = actual)

  def checkExtractedValue[U](expected: U, actual: String => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  JsonResponseRule[U] = JsonResponseRule(expected, actual = actual)

  def given(jsonQuery : String)(rules : JsonResponseRule[_]*) : Seq[ValidationRule[HttpRequestBuilder]] = {
    rules.map(r => r.copy(query = jsonQuery))
  }*/

  def given(jsonQuery : JsonQuery)(fnRule : String => Any) : ValidationRule[HttpRequestBuilder] = {
    JsonResponseRule(query = jsonQuery.jq,fnActual = fnRule)
  }

  implicit object HttpValidationExecutor extends HttpValidator
}


trait HttpRequestConditions {

  def havingJsonQuery(jq : String) : JsonQuery = JsonQuery(jq)



}

case class JsonQuery(jq : String)