package com.jetprobe.core.task.builder

import com.jetprobe.core.task.HttpRequestTask.{HttpRequestMessage, handleHttpRequests, parseHttpRequest}
import com.jetprobe.core.task._
import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.PipelineContext

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */

case object HttpRequestTask extends TaskType

class HttpRequestTaskBuilder(val description : String, requestBuilder : HttpRequestBuilder) extends TaskBuilder {


  val taskMeta = TaskMeta(description,HttpRequestTask)
  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: ExecutableTask): ExecutableTask = {

    new SelfExecutableTask(taskMeta,HttpRequestMessage(requestBuilder),next,ctx.system,ctx.controller)(handleHttp)
  }

  def handleHttp(taskMessage: TaskMessage, session: Session): Session = taskMessage match {
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
          logger.error(s"Failed parsing of the request : [${reqBuilder.requestName}]. The Http Task would be skipped.")
          session
      }
  }
}
