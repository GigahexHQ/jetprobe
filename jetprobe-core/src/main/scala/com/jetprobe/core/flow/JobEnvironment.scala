package com.jetprobe.core.flow

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.session.Session

import scala.concurrent.duration._
import akka.pattern._
import akka.util.Timeout
import com.jetprobe.core.flow.JobController.{GetCurrentEnvVars, UpdateJobEnvVars}
import com.jetprobe.core.flow.ScenarioExecutor.FromUser

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * @author Shad.
  */
object JobEnvironment {

  var system: ActorSystem = null

  implicit val timeout = Timeout(2.seconds)

  val actorsRepo: mutable.Map[String, ActorRef] = mutable.Map.empty

  var currentEnvVars: Map[String, Any] = Map.empty

  def getJobController: Future[ActorRef] = system.actorSelection(s"/user/${JobController.actorName}").resolveOne()

  def saveParam[T <: Any](value: T, name: String): Future[Map[String, Any]] = {
    lazy val jc = getJobController

    for {
      cntrlActor <- jc
      envVars <- cntrlActor.ask(GetCurrentEnvVars).map(_.asInstanceOf[Map[String, Any]]).map(vars => vars ++ Map(name -> value))
    } yield {
      cntrlActor ! UpdateJobEnvVars(envVars, FromUser)
      envVars
    }

  }

  def getVars: Map[String, Any] = {

    val fres = getJobController.flatMap { controllerActor =>
      controllerActor.ask(GetCurrentEnvVars).map(_.asInstanceOf[Map[String, Any]])
    }

    Await.result(fres,2.seconds)

  }

  def getVar[T](key : String) : T = {

     getVars.get(key).get.asInstanceOf[T]


  }


}
