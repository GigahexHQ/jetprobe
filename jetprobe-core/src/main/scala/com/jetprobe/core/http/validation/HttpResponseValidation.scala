package com.jetprobe.core.http.validation

import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.sink.DataSource
import com.jetprobe.core.validations.ValidationRule
import sourcecode.{FullName, Line}

/**
  * @author Shad.
  */

case class HttpResponseValidationRule[U <: Any](expected: U,
                                                httpRequestName: String = "",
                                                actual: (FetchedResponse) => U,
                                                line: Line,
                                                fullName: FullName) extends ValidationRule[HttpRequestBuilder] {

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}"

}

case class FetchedResponse(status: Int, responseBody: String, headers: Map[String, String]) extends DataSource

case class JsonResponseRule[U <: Any](expected: U,
                                      httpRequestName: String = "",
                                      query : String = "",
                                      actual: (String) => U,
                                      line: Line,
                                      fullName: FullName) extends ValidationRule[HttpRequestBuilder] {

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}, with query = ${query}"

}