package com.jetprobe.core.action

import java.nio.charset.StandardCharsets._

import com.jetprobe.core.http._
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.session.Session
import org.asynchttpclient.DefaultAsyncHttpClient

/**
  * @author Shad.
  */
class HttpRequestAction(httpRequestBuilder: HttpRequestBuilder, val next: Action) extends Action {
  override def name: String = "httpRequestAction-" + httpRequestBuilder.uri

  override def execute(session: Session): Unit = {

      logger.debug(s"session while triggering http action : ${session.attributes}")
      val exprParser = new ExpressionParser(session.attributes)
      logger.info(s"passed uri : ${httpRequestBuilder.uri}")
      ExpressionParser.parse(httpRequestBuilder.uri, session.attributes) match {
        case Some(httpURI) =>
          val httpResponse = handleHttpRequest(httpRequestBuilder, httpURI)
          logger.info(s"executing http action using  uri : $httpURI")
          val savedVariables = httpRequestBuilder.responseInfoExtractor match {
            case Some(extractors) => extractors.flatMap(jsonBuilder => jsonBuilder.extractFrom(httpResponse.body.string)).toMap
            case None =>

              Map.empty[String, Any]
          }
          val updatedSession = session.copy(attributes = session.attributes ++ savedVariables)
          next ! updatedSession

        case None =>
          logger.error(s"Failed parsing of the uri : ${httpRequestBuilder.uri}. The http action ${httpRequestBuilder.requestName} would be skipped")
          next ! session
      }

  }

  private[this] def handleHttpRequest(reqBuilder: HttpRequestBuilder, parsedURI: String): HttpResponse = {
    val asyncHttpClient = new DefaultAsyncHttpClient
    val requestBuilder = httpRequestBuilder.method match {
      case GET => asyncHttpClient.prepareGet(parsedURI)
      case POST => asyncHttpClient.preparePost(parsedURI).setBody(httpRequestBuilder.body.get)

    }
    httpRequestBuilder.headers.foreach(hd => requestBuilder.addHeader(hd._1, hd._2))
    val request = requestBuilder.build()
    val response = requestBuilder.execute().get()
    asyncHttpClient.close()
    HttpResponse(request, response.getHeaders, new StringResponseBody(response.getResponseBody, UTF_8), Some(response.getStatusCode))

  }
}
