package com.jetprobe.core.http

import com.jetprobe.core.extractor.JsonPathBuilder

import scala.io.Source

/**
  * @author Shad.
  */
case class Http(requestName: String) {

  def get(url: String): HttpRequestBuilder = httpRequest(requestName, GET, url)
  def post(url : String) : HttpRequestBuilder = httpRequest(requestName,POST,url)

  def httpRequest(name: String, method: RequestType, url: String): HttpRequestBuilder = HttpRequestBuilder(name, uri = url, method = method)

}
object Http {
  type ResponseInfoExtractor = Response => JsonPathBuilder
}
trait HttpSupport {
  //Required for buildign the body for json payload
  def fromFile(filePath : String) : String = Source.fromFile(filePath).mkString

}
