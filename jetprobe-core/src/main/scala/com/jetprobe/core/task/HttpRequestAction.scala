package com.jetprobe.core.task

import java.nio.charset.StandardCharsets._

import com.jetprobe.core.http._
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.session.Session
import org.asynchttpclient._
import ExpressionParser._
import HttpRequestTask._
import akka.actor.{ActorRef, Props}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */


object HttpRequestTask {

  case class HttpRequestMessage(reqBuilder : HttpRequestBuilder) extends TaskMessage {

    override def name: String = s"Http request : ${reqBuilder.requestName}"
  }

  private[task] def parseHttpRequest(rb: HttpRequestBuilder, attributes: Map[String, Any]): Option[HttpRequestBuilder] = {

    val uriAndHeaderParsed = parse(rb.uri, attributes).flatMap(parsedURI => {
      val modHttp = rb.copy(uri = parsedURI)

      val parsedHeaders = modHttp.headers.map {
        case (k, value) => k -> parse(value, attributes)
      }
      if (parsedHeaders.count(_._2.isEmpty) > 0)
        None
      else {
        val retrievedHeads = parsedHeaders.map {
          case (k, v) => k -> v.get
        }
        Some(modHttp.copy(headers = retrievedHeads))
      }
    })

    val body = rb.body.flatMap { content =>
      val strippedBody = content.split('\n').map(_.trim.filter(_ >= ' ')).mkString
      parse(strippedBody, attributes)
    }
    uriAndHeaderParsed.map(req => req.copy(body = body))

  }

  def handleHttpRequests(reqBuilder: HttpRequestBuilder, parsedURI: String): HttpResponse = {

    val asyncHttpClient = if (reqBuilder.uri.startsWith("https")) {
      getHttpsClient
    }

    else
      new DefaultAsyncHttpClient


    val requestBuilder = reqBuilder.method match {
      case GET => asyncHttpClient.prepareGet(parsedURI)
      case POST => asyncClientWithBody(asyncHttpClient.preparePost(parsedURI), reqBuilder.body)
        val asyncClient = asyncHttpClient.preparePost(parsedURI)
        if (reqBuilder.body.nonEmpty)
          asyncClient.setBody(reqBuilder.body.get)
        else
          asyncClient
      case DELETE => asyncHttpClient.prepareDelete(parsedURI)
      case PUT =>
        asyncClientWithBody(asyncHttpClient.preparePut(parsedURI), reqBuilder.body)


    }
    reqBuilder.headers.foreach(hd => requestBuilder.addHeader(hd._1, hd._2))
    val request = requestBuilder.build()
    val response = requestBuilder.execute().get()
    asyncHttpClient.close()
    HttpResponse(request, response.getHeaders, new StringResponseBody(response.getResponseBody, UTF_8), Some(response.getStatusCode))

  }

  def asyncClientWithBody(clientBuidler: BoundRequestBuilder, body: Option[String]): BoundRequestBuilder = {
    if (body.nonEmpty) clientBuidler.setBody(body.get) else clientBuidler
  }

  def getHttpsClient: AsyncHttpClient = {
    val config = new DefaultAsyncHttpClientConfig.Builder().setUseInsecureTrustManager(true)
    Dsl.asyncHttpClient(config)
  }

}

