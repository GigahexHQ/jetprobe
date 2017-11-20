package com.jetprobe.core.http.validation

import com.jetprobe.core.action.HttpRequestAction
import com.jetprobe.core.extractor.JsonPathBuilder
import com.jetprobe.core.http.{HttpRequestBuilder, HttpResponse}
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

/**
  * @author Shad.
  */
class HttpValidator extends ValidationExecutor[HttpRequestBuilder] with LazyLogging {

  def handleHttpRequest(sink: HttpRequestBuilder, config: Map[String, Any]): Option[HttpResponse] = {
    ExpressionParser.parse(sink.uri, config) match {
      case Some(httpURI) =>
        Some(HttpRequestAction.handleHttpRequests(sink, httpURI))
      case None =>
        logger.error(s"Unable to extract request URI for variable ${sink.uri}")
        None
    }
  }

  override def execute(rules: Seq[ValidationRule[HttpRequestBuilder]], sink: HttpRequestBuilder, config: Map[String, Any]): Seq[ValidationResult] = {

    //fetch the response
    val httpResponse = config.get(sink.requestName) match {
      case Some(HttpResponse(a, b, c, d)) => Some(getHttpResponse(HttpResponse(a, b, c, d)))
      case Some(_) => None
      case None =>
        handleHttpRequest(sink, config).map(getHttpResponse(_))

    }

    httpResponse match {
      case Some(resp) =>
        rules map {
          case HttpResponseValidationRule(expected, httpRequestName, actual, line, fullName) =>
            if(expected == actual.apply(resp))
              ValidationResult.success(HttpResponseValidationRule(expected, httpRequestName, actual, line, fullName))
            else {
              val message = s"Expected = $expected , Actual : ${actual.apply(resp)}"
              ValidationResult.failed(HttpResponseValidationRule(expected, httpRequestName, actual, line, fullName),message)
            }

          case rule : JsonResponseRule[_] =>
            val jsonExtractor = new JsonPathBuilder(rule.query,"temp.out").extractFrom(resp.responseBody).get("temp.out").get.toString
            if(rule.expected == rule.actual.apply(jsonExtractor))
              ValidationResult.success(rule)
            else {
              ValidationResult.failed(rule,ValidationResult.getFailedMessage(rule.expected,rule.actual.apply(jsonExtractor)))
            }

        }

      case None =>
        rules.map(ValidationResult.skipped(_,"Unable to fetch http response."))

    }



  }


  private[this] def getHttpResponse(response: HttpResponse): FetchedResponse = {
    val mapHeaders = response.headers.entries().asScala.toList.flatMap(x => Map(x.getKey -> x.getValue)).toMap
    val status = response.statusCode.getOrElse(404)
    val body = response.body.string
    FetchedResponse(status, body, mapHeaders)
  }

}
