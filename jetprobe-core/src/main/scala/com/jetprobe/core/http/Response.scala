package com.jetprobe.core.http

import java.nio.charset.Charset

import io.netty.handler.codec.http.HttpHeaders
import org.asynchttpclient.{HttpResponseStatus, Request => AHCRequest}
import org.asynchttpclient.netty.request.NettyRequest
import org.asynchttpclient.uri.Uri
import scala.collection.JavaConverters._
import io.netty.handler.codec.http.cookie.{ ClientCookieDecoder, Cookie }

/**
  * @author Shad.
  */
abstract class Response {

  def request: AHCRequest
  def statusCode: Option[Int]

  def header(name: String): Option[String]
  def headers: HttpHeaders
  def headers(name: String): Seq[String]
  def cookies: List[Cookie]

  def body: ResponseBody


}

case class HttpResponse(
                         request:      AHCRequest,
                         headers:      HttpHeaders,
                         body:         ResponseBody,
                         statusCode : Option[Int]
                       ) extends Response {



  def header(name: String): Option[String] = Option(headers.get(name))
  def headers(name: String): Seq[String] = headers.getAll(name).asScala

  lazy val cookies = headers.getAll(HeaderNames.SetCookie).asScala.flatMap(setCookie => Option(ClientCookieDecoder.LAX.decode(setCookie))).toList

}