package com.jetprobe.core.http.validation

import com.jetprobe.core.task.HttpRequestTask
import com.jetprobe.core.extractor.JsonPathBuilder
import com.jetprobe.core.http.{HttpRequestBuilder, HttpResponse}
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.storage.DataSource
import com.jetprobe.core.validations.ValidationExecutor.Parsed
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.typesafe.scalalogging.LazyLogging
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */

trait HttpRequestPropertyFetcher[T] {

  def fetch(response: FetchedResponse): Parsed[T]

}

case class HttpResponseValidationRule(httpRequestName: String = "",
                                      actual: (FetchedResponse) => Any) extends ValidationRule[HttpRequestBuilder] {

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}"

  override def validate(config: Map[String, Any], storage: HttpRequestBuilder): ValidationResult = ???
}

case class FetchedResponse(status: Int, responseBody: String, headers: Map[String, String]) extends DataSource

case class JsonResponseRule[T](httpRequestName: String = "",
                            query: String,
                            fnActual: T => Any) extends ValidationRule[HttpRequestBuilder]
  with HttpRequestPropertyFetcher[T]
  with ValidationExecutor[HttpRequestBuilder]
  with LazyLogging {

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}, with query = ${query}"

  override def fetch(response: FetchedResponse): Parsed[T] = {

    val jsonExtractor = Try {
      new JsonPathBuilder(query, "temp.out").extractFrom[T](response.responseBody).get("temp.out").get
    }

    val result = jsonExtractor match {
      case Success(v) => Right(v)
      case Failure(ex) => Left(ex)
    }

    Future(result)
  }

  override def validate(config: Map[String, Any], storage: HttpRequestBuilder): ValidationResult = {

    val httpResponse = config.get(httpRequestName) match {
      case Some(HttpResponse(a, b, c, d)) => Some(getHttpResponse(HttpResponse(a, b, c, d)))
      case Some(_) => None
      case None =>
        handleHttpRequest(storage, config).map(getHttpResponse(_))

    }

    httpResponse match {
      case Some(response) => validateResponse[T](fetch(response), fnActual)
      case None => ValidationResult.skipped(httpRequestName, " Unable to fetch http response.")
    }
  }

  private[this] def getHttpResponse(response: HttpResponse): FetchedResponse = {
    val mapHeaders = response.headers.entries().asScala.toList.flatMap(x => Map(x.getKey -> x.getValue)).toMap
    val status = response.statusCode.getOrElse(404)
    val body = response.body.string
    FetchedResponse(status, body, mapHeaders)
  }

  def handleHttpRequest(sink: HttpRequestBuilder, config: Map[String, Any]): Option[HttpResponse] = {
    ExpressionParser.parse(sink.uri, config) match {
      case Some(httpURI) =>
        val response = HttpRequestTask.handleHttpRequests(sink, httpURI)
        Some(response)
      case None =>
        logger.error(s"Unable to extract request URI for variable ${sink.uri}")
        None
    }
  }
}