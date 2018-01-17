package com.jetprobe.core.http

import java.io.File

import com.jetprobe.core.extractor.JsonPathBuilder
import com.jetprobe.core.http.validation.HttpRequestConditions
import com.jetprobe.core.storage.DataSource

import scala.collection.mutable

/**
  * @author Shad.
  */
case class HttpRequestBuilder(
                               requestName : String,
                               headers: mutable.Map[String, String] = mutable.Map.empty,
                               uri : String,
                               method : RequestType,
                               body: Option[String] = None,
                               responseInfoExtractor: Option[Seq[JsonPathBuilder]] = None
                             ) extends DataSource with HttpRequestConditions{

  def body(bd: String): HttpRequestBuilder =  this.copy(body = Some(bd))

  def header(key: String, value: String): HttpRequestBuilder = this.copy(headers = headers ++ Map(key -> value))

  def extract(infoExtractor: JsonPathBuilder*): HttpRequestBuilder = {
   val seqExtractors = responseInfoExtractor match {
      case Some(extractors) => extractors ++ infoExtractor
      case None => infoExtractor
    }
    this.copy(responseInfoExtractor = Some(seqExtractors))
  }


}
