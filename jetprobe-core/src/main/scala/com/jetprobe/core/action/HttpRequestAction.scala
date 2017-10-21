package com.jetprobe.core.action

import java.nio.charset.StandardCharsets._
import com.jetprobe.core.http._
import com.jetprobe.core.session.Session
import org.asynchttpclient.DefaultAsyncHttpClient

/**
  * @author Shad.
  */
class HttpRequestAction(httpRequestBuilder: HttpRequestBuilder, val next: Action) extends Action {
  override def name: String = "httpRequestAction-" + httpRequestBuilder.uri

  override def execute(session: Session): Unit = {


    val asyncHttpClient = new DefaultAsyncHttpClient
    val requestBuilder = httpRequestBuilder.method match {
      case GET => asyncHttpClient.prepareGet(httpRequestBuilder.uri)
      case POST => asyncHttpClient.preparePost(httpRequestBuilder.uri).setBody(httpRequestBuilder.body.get)

    }


    httpRequestBuilder.headers.foreach(hd => requestBuilder.addHeader(hd._1, hd._2))
    val request = requestBuilder.build()
    val response = requestBuilder.execute().get()
    val httpResponse = HttpResponse(request, response.getHeaders, new StringResponseBody(response.getResponseBody, UTF_8), Some(response.getStatusCode))
    asyncHttpClient.close()
    val savedVariables = httpRequestBuilder.responseInfoExtractor match {
      case Some(extractors) => extractors.flatMap(jsonBuilder => jsonBuilder.extractFrom(httpResponse.body.string)).toMap
      case None => Map.empty[String, Any]
    }
    println(s"before attributes saved ${session.attributes}")
    val updatedSession = session.copy(attributes = session.attributes ++ savedVariables)
    println(s"attributes saved ${updatedSession.attributes}")
    next ! updatedSession
  }
}
