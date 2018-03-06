package com.jetprobe.core.http

import java.io.File

import com.jetprobe.core.extractor.JsonPathBuilder
import com.jetprobe.core.http.validation.HttpRequestConditions
import com.jetprobe.core.storage.{DataSource, Storage}
import com.jetprobe.core.structure.Config

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
                             ) extends Storage with HttpRequestConditions with Config[HttpRequestBuilder]{

  def body(bd: String): HttpRequestBuilder =  this.copy(body = Some(bd))

  def header(key: String, value: String): HttpRequestBuilder = this.copy(headers = headers ++ Map(key -> value))

  def extract(infoExtractor: JsonPathBuilder*): HttpRequestBuilder = {
   val seqExtractors = responseInfoExtractor match {
      case Some(extractors) => extractors ++ infoExtractor
      case None => infoExtractor
    }
    this.copy(responseInfoExtractor = Some(seqExtractors))
  }

  override private[jetprobe] def getStorage(sessionConf: Map[String, Any]) = this
}
