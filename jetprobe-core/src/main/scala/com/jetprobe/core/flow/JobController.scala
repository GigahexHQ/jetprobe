package com.jetprobe.core.flow

import java.io.File

import akka.actor.{ActorRef, Props}
import cats.syntax.either._
import com.jetprobe.core.flow.JobController._
import com.jetprobe.core.flow.ScenarioExecutor._
import com.jetprobe.core.generator.ActorNameGenerator
import com.jetprobe.core.structure.PipelineBuilder
import com.jetprobe.core.task._
import io.circe.yaml.parser
//import io.circe._
//import io.circe.syntax._
import com.jetprobe.core.flow.JobDescriptors._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * @author Shad.
  */

class JobController(scenarios: mutable.Queue[ScenarioMeta], classLoader: Option[ClassLoader] = None) extends BaseActor {

  val clsLoader = classLoader match {
    case Some(x) => x
    case None => getClass.getClassLoader
  }

  var envVars : Map[String,Any] = Map.empty

  override def receive: Receive = {
    case StartJobExecution =>
      val scenario = scenarios.dequeue()
      val queueScns = mutable.Queue(scenario.pipelines.map(p =>  PipelineBuilder(p.name,p.className)))
      val scnExecutor = ScenarioExecutor.buildScenario(scenario.pipelines,clsLoader).map(scn => ScenarioExecutor.props(scenario.name,scn,self))
      scnExecutor match {
        case Left(th) => self ! StopJobExecution(Failed,th.getMessage)
        case Right(props) =>
          val scnActor = context.actorOf(props,ActorNameGenerator.getName(scenario.name))
          scnActor ! StartScenarioExecution(scenario.params)
      }

    case ScenarioCompleted(name,metrics,status) =>
      logger.info(s"Job completed ")
      self ! StopJobExecution(status,"Success")


    case StopJobExecution(status,message) =>
      logger.info(s"Job completed with status : ${status.toString} and message : ${message}")
      stopChildren
      context stop self
      context.system.terminate().onComplete({
        case _ =>
          logger.info("Actor system shutdown")
          //System.exit(0)
      })

    case UpdateJobEnvVars(vars,eventSource) =>
      envVars = envVars ++ vars
      eventSource match {
        case FromUser => context.children.foreach(actor => actor ! UpdateScnEnvVariables(vars,FromJobController))
        case _ => logger.debug("Received update for vars")
      }

    case GetCurrentEnvVars =>
      sender() ! envVars

  }


}

object JobController {

  val actorName = "JobController"

  type JobParams = (mutable.Queue[ScenarioMeta], Option[ClassLoader])

  def props(scenarios: mutable.Queue[ScenarioMeta], classLoader: Option[ClassLoader]) : Props = Props(new JobController(scenarios,classLoader))

  case object StartJobExecution

  case class UpdateJobEnvVars(vars : Map[String,Any],eventSource: EventSource)

  case object GetCurrentEnvVars

  case class ScenarioCompleted(name : String, metrics : ArrayBuffer[PipelineStats],status : RunStatus)

  case class StopJobExecution(status : RunStatus, message : String = "Completed Job")


  def buildFromConfig(path: String, optClassLoader : Option[ClassLoader]): Either[Throwable, JobParams] = {
    val externalFile = new File(path)
    val yamlString: Option[String] = externalFile match {
      case f if (f.isFile) => Some(Source.fromFile(f).getLines().toSeq.mkString("\n"))
      case f if (!f.isFile) =>
        //check for resource path
        val fileStream = getClass.getResourceAsStream("/"+path)
        fileStream match {
          case fs if (fs == null) => None
          case _ =>
            val lines = Source.fromInputStream(fileStream).getLines().toSeq.mkString("\n")
            Some(lines)
        }

      case _ => None
    }

     yamlString match {
      case Some(yaml) =>
         parser.parse(yaml)
          .leftMap(err => err.underlying)
          .flatMap{ js =>
          val extractScn = js.as[ScenarioMeta]
          extractScn.map(v => (mutable.Queue(v),optClassLoader))

        }

      case None =>
        Left(new Exception(s"Unable to parse file at ${path}"))

    }

  }

}
