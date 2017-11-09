package com.jetprobe.core.http.validation

import com.jetprobe.core.http.HttpRequestBuilder

/**
  * @author Shad.
  */
object HttpValidationSupport {

  implicit def httpRequestToRuleBuilder(httpRequestBuilder: HttpRequestBuilder) : HttpValidationRuleBuilder = new HttpValidationRuleBuilder(httpRequestBuilder)


  def checkHttpResponse[U](expected: U, actual: FetchedResponse => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  HttpResponseValidationRule[U] = HttpResponseValidationRule(expected, actual = actual, fullName = fullName, line = line)

  def checkExtractedValue[U](expected: U, actual: String => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  JsonResponseRule[U] = JsonResponseRule(expected, actual = actual, fullName = fullName, line = line)

  implicit object HttpValidationExecutor extends HttpValidator
}
