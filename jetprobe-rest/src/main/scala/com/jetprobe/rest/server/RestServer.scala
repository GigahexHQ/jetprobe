package com.jetprobe.rest.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.jetprobe.rest.services.JobSubmissionService

/**
  * @author Shad.
  */
object RestServer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("jetprobe-rest")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val routes =
       (new JobSubmissionService().submit)
    Http().bindAndHandle(routes, "localhost", 12345)

    println("started the server")

  }

}
