package com.jetprobe.core.flow

import java.lang.reflect.Modifier

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.flow.JobController.{ScenarioCompleted, UpdateJobEnvVars}
import com.jetprobe.core.flow.JobDescriptors.{Pipeline, PipelineStats}
import com.jetprobe.core.runner.PipelineManager
import com.jetprobe.core.runner.PipelineManager.{GetRunningStats, StartPipelineExecution, UpdatePipelineEnvVars}
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.{ExecutablePipeline, PipelineBuilder}
import com.jetprobe.core.task.{BaseActor, RunStatus, TaskMetrics}
import scala.concurrent.duration._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class ScenarioExecutor(name : String, pipes: mutable.Queue[ExecutablePipeline], controller: ActorRef) extends BaseActor {

  var envVars : Map[String,Any] = _

  import ScenarioExecutor._

  val pipelinesMetrics : ArrayBuffer[PipelineStats] = ArrayBuffer.empty

  override def receive: Receive = {
    case StartScenarioExecution(params) =>
      //create the pipeline actor
      val pipeline = pipes.dequeue()
      val pipeActor = context.actorOf(PipelineManager.props(pipeline.copy(config = pipeline.config ++ params),self))
      pipeActor ! StartPipelineExecution

    case PipelineComplete(stats,session,status) =>

      pipelinesMetrics += stats
      if(pipes.isEmpty) {
        controller ! ScenarioCompleted(name,pipelinesMetrics,status)
      } else self ! StartScenarioExecution(session.attributes)


    case UpdateScnEnvVariables(vars,eventSource) =>
      envVars = vars
      eventSource match {
        case FromPipelineManager => controller ! UpdateJobEnvVars(vars,eventSource)
        case FromJobController => context.children.foreach(actor => actor ! UpdatePipelineEnvVars(vars,FromScenarioExecutor))
      }

    case GetCurrentPipelineStats =>
      implicit val timeout = Timeout(1.seconds)
      val result = context.children.map{ child =>
        val res = child ? GetRunningStats
        res.mapTo[PipelineStats]
      }

        Future.sequence(result).onComplete {
          case Success(xs) => sender() ! pipelinesMetrics.toList ::: xs.toList
          case Failure(ex) => logger.error(s"Exception occurred while getting current pipeline stats : ${ex.getMessage}")
        }





  }



}

object ScenarioExecutor {

  trait EventSource
  case object FromJobController extends EventSource
  case object FromPipelineManager extends EventSource
  case object FromScenarioExecutor extends EventSource
  case object FromUser extends EventSource

  case class StartScenarioExecution(params: Map[String,Any])
  case class PipelineComplete(stats : PipelineStats, currentSession : Session,status : RunStatus)
  case class UpdateScnEnvVariables(vars : Map[String,Any], eventSource: EventSource)
  case object GetCurrentPipelineStats


  def props(name : String, pipes: mutable.Queue[ExecutablePipeline], controller: ActorRef) : Props = Props(new ScenarioExecutor(name,pipes,controller))


  def buildScenario(pipeDefs: Seq[Pipeline], classLoader: ClassLoader): Either[Throwable, mutable.Queue[ExecutablePipeline]] = {
    val execPipes = pipeDefs.map { pipe =>
        buildPipe(pipe,classLoader)
    }

    execPipes.count(opt => opt.isLeft) > 0 match {
      case true => Left(execPipes.takeWhile(_.isLeft).head.left.get)
      case false =>
        val pipelineQueue : mutable.Queue[ExecutablePipeline] = mutable.Queue.empty
        pipelineQueue ++= execPipes.flatMap(_.right.get)
        Right(pipelineQueue)
    }
  }

  private def buildPipe(pipeDef: Pipeline, classLoader: ClassLoader): Either[Throwable, Array[ExecutablePipeline]] = {
    val pipeBuilder = Try(classLoader.loadClass(pipeDef.className)) match {
      case Success(cls) => Success(cls)
      case Failure(ex) => Failure(new Exception(s"Unable to find the class : ${ex.getMessage}"))
    }

    val result = pipeBuilder.map { cls =>

      val isAbstract = Modifier.isAbstract(cls.getModifiers)
      (cls.isAnnotationPresent(classOf[PipelineMeta]) && !isAbstract) match {
        case true =>
          val t = cls.newInstance()

          if (t.isInstanceOf[TestPipeline]) {
            val m = cls.getDeclaredMethod("tasks")
            m.setAccessible(true)
            val result = m.invoke(t).asInstanceOf[PipelineBuilder]
            val execPipe = result.build()
            val builtPipe = execPipe.copy(className = pipeDef.className, config = pipeDef.params)
            val arrPipes = {
              if(pipeDef.repeat > 1)
                Array.fill[ExecutablePipeline](pipeDef.repeat)(builtPipe)
              else
                Array(builtPipe)

            }

            Right(arrPipes)
          }

          else Left(new Exception("Class Must extend TestPipeline"))

        case false => Left(new Exception("Class must have 'PipelineMeta' Annotation and should not be abstract"))

      }

    }

    result match {
      case Success(r) => r
      case Failure(ex) => Left(ex)
    }

  }
}