package com.jetprobe.core.action.builder

import com.jetprobe.core.action.HttpRequestAction.{HttpRequestMessage, handleHttpRequests, parseHttpRequest}
import com.jetprobe.core.action._
import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.ScenarioContext

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class HttpRequestActionBuilder(requestBuilder : HttpRequestBuilder) extends ActionBuilder {

  val name : String = s"HttpAction-${requestBuilder.requestName.replaceAll(" ","_")}"
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    new SelfExecutableAction(name,HttpRequestMessage(requestBuilder),next,ctx.system,ctx.controller)(handleHttp)
  }

  def handleHttp(actionMessage: ActionMessage, session: Session): Session = actionMessage match {
    case HttpRequestMessage(reqBuilder) =>
      val parsedReq = parseHttpRequest(reqBuilder, session.attributes)

      parsedReq match {
        case Some(httpRequest) =>
          val httpResponse = Try {
            handleHttpRequests(httpRequest, httpRequest.uri)
          }

          httpResponse match {
            case Failure(ex) =>
              logger.error(s"Http Request : [${httpRequest.uri}] failed with exception : ${ex}")
              session

            case Success(response) =>

              if(response.statusCode.get == 400 || response.statusCode.get == 404)
                logger.error(s"Error response : ${response.body.string}")
              val savedVariables = httpRequest.responseInfoExtractor match {

                case Some(extractors) => extractors.flatMap(jsonBuilder =>
                  jsonBuilder.extractFrom(response.body.string)).toMap

                case None =>
                  Map.empty[String, Any]
              }

              val updatedSession = session.copy(attributes = session.attributes ++ savedVariables)
              updatedSession

          }

        case None =>
          logger.error(s"Failed parsing of the request : [${reqBuilder.requestName}]. The Http Action would be skipped.")
          session
      }
  }
}
