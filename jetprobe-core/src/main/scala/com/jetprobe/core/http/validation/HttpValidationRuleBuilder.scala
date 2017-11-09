package com.jetprobe.core.http.validation

import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.validations.{ValidationRule, ValidationRulesBuilder}

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class HttpValidationRuleBuilder(httpSink : HttpRequestBuilder) extends ValidationRulesBuilder[HttpRequestBuilder]{

  def forHttpRequest(ruleBuilders : HttpResponseValidationRule[_]*) : Seq[ValidationRule[HttpRequestBuilder]] = {

    ruleBuilders.map(rule => rule.copy(httpRequestName = httpSink.requestName))

  }

  def forJsonQuery(query : String)(ruleBuilders : JsonResponseRule[_]*) : Seq[ValidationRule[HttpRequestBuilder]] = {

    ruleBuilders.map(rule => rule.copy(query = query, httpRequestName = httpSink.requestName))

  }

  override def build: ArrayBuffer[ValidationRule[HttpRequestBuilder]] = ???

}
