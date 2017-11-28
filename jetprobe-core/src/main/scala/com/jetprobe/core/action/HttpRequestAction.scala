package com.jetprobe.core.action

import java.nio.charset.StandardCharsets._

import com.jetprobe.core.http._
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.session.Session
import org.asynchttpclient._
import ExpressionParser._
import HttpRequestAction._
import akka.actor.{ActorRef, Props}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class HttpRequestAction(httpExecutor: ActorRef,httpRequestBuilder: HttpRequestBuilder) extends Action {

  override def name: String = "httpRequestAction"

  override def execute(session: Session): Unit = httpExecutor ! HttpRequestMessage(httpRequestBuilder)

  /* override def execute(session: Session): Unit = {

     val exprParser = new ExpressionParser(session.attributes)
     val parsedReq = parseHttpRequest(httpRequestBuilder, session.attributes)

     parsedReq match {
       case Some(httpRequest) =>
         val httpResponse = Try {
           handleHttpRequests(httpRequest, httpRequest.uri)
         }

         httpResponse match {
           case Failure(ex) =>
             logger.error(s"Http Request : [${httpRequest.uri}] failed with exception : ${ex}")
             next ! session

           case Success(response) =>
             //logger.info(s"received response : ${response.body.string}")
             logger.info(s"Response status : ${response.statusCode.get}. Http Request [${httpRequestBuilder.method.toString}]: ${httpRequest.uri}")
             if(response.statusCode.get == 400 || response.statusCode.get == 404)
               logger.error(s"Error response : ${response.body.string}")
             val savedVariables = httpRequest.responseInfoExtractor match {

               case Some(extractors) => extractors.flatMap(jsonBuilder =>
                 jsonBuilder.extractFrom(response.body.string)).toMap

               case None =>
                 Map.empty[String, Any]
             }

             val updatedSession = session.copy(attributes = session.attributes ++ savedVariables)

             next ! updatedSession


         }

       case None =>
         logger.error(s"Failed parsing of the request : [${httpRequestBuilder.requestName}]. The Http Action would be skipped.")
         next ! session
     }

   }*/

}

class HttpExecutor(val next : Action) extends ActionActor {

  override def execute(actionMessage: ActionMessage, session: Session): Unit = actionMessage match {
    case HttpRequestMessage(reqBuilder) =>
      val exprParser = new ExpressionParser(session.attributes)
      val parsedReq = parseHttpRequest(reqBuilder, session.attributes)

      parsedReq match {
        case Some(httpRequest) =>
          val httpResponse = Try {
            handleHttpRequests(httpRequest, httpRequest.uri)
          }

          httpResponse match {
            case Failure(ex) =>
              logger.error(s"Http Request : [${httpRequest.uri}] failed with exception : ${ex}")
              next ! session

            case Success(response) =>
              //logger.info(s"received response : ${response.body.string}")
              logger.info(s"Response status : ${response.statusCode.get}. Http Request [${reqBuilder.method.toString}]: ${httpRequest.uri}")
              if(response.statusCode.get == 400 || response.statusCode.get == 404)
                logger.error(s"Error response : ${response.body.string}")
              val savedVariables = httpRequest.responseInfoExtractor match {

                case Some(extractors) => extractors.flatMap(jsonBuilder =>
                  jsonBuilder.extractFrom(response.body.string)).toMap

                case None =>
                  Map.empty[String, Any]
              }

              val updatedSession = session.copy(attributes = session.attributes ++ savedVariables)

              next ! updatedSession


          }

        case None =>
          logger.error(s"Failed parsing of the request : [${reqBuilder.requestName}]. The Http Action would be skipped.")
          next ! session
      }
  }
}

object HttpExecutor {

  def props(next : Action) : Props = Props(new HttpExecutor(next))
}

object HttpRequestAction {

  case class HttpRequestMessage(reqBuilder : HttpRequestBuilder) extends ActionMessage {

    override def name: String = s"Http request : ${reqBuilder.requestName}"
  }

  private[action] def parseHttpRequest(rb: HttpRequestBuilder, attributes: Map[String, Any]): Option[HttpRequestBuilder] = {

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

