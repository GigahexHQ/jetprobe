package com.jetprobe.core.http.validation

import com.jetprobe.core.extractor.JsonPathBuilder
import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.storage.DataSource
import com.jetprobe.core.validations.ValidationExecutor.Parsed
import com.jetprobe.core.validations.{ValidationResult, ValidationRule}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */

trait HttpRequestPropertyFetcher[T] {

  def fetch(response : FetchedResponse) : Parsed[T]

}

case class HttpResponseValidationRule(httpRequestName: String = "",
                                                actual: (FetchedResponse) => Any) extends ValidationRule[HttpRequestBuilder] {

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}"

  override def validate(config: Map[String, Any], storage: HttpRequestBuilder): ValidationResult = ???
}

case class FetchedResponse(status: Int, responseBody: String, headers: Map[String, String]) extends DataSource

case class JsonResponseRule(httpRequestName: String = "",
                                      query : String ,
                            fnActual: (String) => Any) extends ValidationRule[HttpRequestBuilder] with HttpRequestPropertyFetcher[String]{

  override def name: String = s"Validation for HTTP Request : ${httpRequestName}, with query = ${query}"

  override def fetch(response : FetchedResponse) : Parsed[String] = {

    val jsonExtractor = Try {
     new JsonPathBuilder(query,"temp.out").extractFrom(response.responseBody).get("temp.out").get.toString
    }

    val result = jsonExtractor match {
      case Success(v) => Right(v)
      case Failure(ex) => Left(ex)
    }

    Future(result)
  }

  override def validate(config: Map[String, Any], storage: HttpRequestBuilder): ValidationResult = ???
}