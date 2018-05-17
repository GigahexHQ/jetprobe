package com.jetprobe.rest.services

import java.io.File
import java.net.URLClassLoader

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives
import com.jetprobe.core.flow.JobController
import javax.ws.rs.Path

import com.jetprobe.rest.server.Routes

import scala.concurrent.ExecutionContext
import io.swagger.annotations._
import Routes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.jetprobe.core.flow.JobController.StartJobExecution
import com.jetprobe.core.flow.JobDescriptors.ScenarioMeta
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.collection.mutable


/**
  * @author Shad.
  */
@Api(value = "/api/v1/job", produces = "application/json")
@Path("/api/v1/job")
class JobSubmissionService(implicit executionContext: ExecutionContext, system : ActorSystem) extends Directives {


  @ApiOperation(value = "Add integers", nickname = "addIntegers", httpMethod = "POST", response = classOf[String])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "\"numbers\" to sum", required = true,
      dataTypeClass = classOf[JobSubmitRequest], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def submit = path(apiPrefix / "jobs") {

    post {
      entity(as[JobSubmitRequest]) { request =>
        val classURL = new File(request.jarPath).toURI.toURL
        val classLoader = new URLClassLoader(Array(classURL), Thread.currentThread().getContextClassLoader())
        val jc = JobController.props("",mutable.Queue(request.job), Some(classLoader))
        val jcActor = system.actorOf(jc)
        jcActor ! StartJobExecution
        println(request.jarPath)
        complete {
          JobSubmitResult("success",1L)
        }
        //complete(HttpEntity(ContentTypes.`application/json`, JobSubmitResult("success",100L)))

      }
    }

  }

}

case class JobSubmitResult(status : String, jobId : Long)

case class JobSubmitRequest(jarPath: String, job: ScenarioMeta)

class JobRequestHandler(jobSubmitRequest: JobSubmitRequest, )