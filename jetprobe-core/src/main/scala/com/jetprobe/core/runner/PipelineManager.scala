package com.jetprobe.core.runner

import java.util.Date

import akka.actor.{ActorRef, PoisonPill, Props}
import com.jetprobe.core.task._
import com.jetprobe.core.controller.ControllerCommand.EndScenario
import com.jetprobe.core.flow.JobDescriptors.PipelineStats
import com.jetprobe.core.flow.ScenarioExecutor.{EventSource, FromPipelineManager, PipelineComplete, UpdateScnEnvVariables}
import com.jetprobe.core.generator.ActorNameGenerator
import com.jetprobe.core.runner.PipelineManager._
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.{ExecutablePipeline, Scenario}
import com.jetprobe.core.task
import com.jetprobe.core.validations.{Failed, Passed, Skipped}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration

/**
  * @author Shad.
  */
class PipelineManager(runnableScn: ExecutablePipeline, controller: ActorRef, name : String) extends BaseActor {

  var startTime: Date = new Date()
  var session: Session = _
  val metricsReport : ArrayBuffer[TaskMetrics] = ArrayBuffer.empty
  val id = ActorNameGenerator.getName(name)


  override def receive: Receive = {

    case StartPipelineExecution =>
      startTime = new Date()
      val scn = runnableScn.build(context.system, new Exit(self), self)
      session = Session(scn.name, scn.className, attributes = scn.configAttr)
      scn.entry.execute(session)

    case UpdatePipelineEnvVars(vars,_) =>
        session = session.copy(attributes = session.attributes ++ vars)

    case ExecuteNext(task, newSession, isScheduled,metrics) =>
      if (!isScheduled) {
        context stop (sender())
      }
      session = newSession
      controller ! UpdateScnEnvVariables(session.attributes,FromPipelineManager)
      metricsReport.+=(metrics)
      task.execute(session)

    case ScenarioCompleted(finalSession) =>

      val status = if (finalSession.validationResults.count(_.status == Passed) == finalSession.validationResults.size) {
        Passed
      } else if (finalSession.validationResults.filter(_.status == Skipped).size > 0) {
        Skipped
      } else Failed


      //Display the tasks stats
      println("******************************************************************")
      println(s"Tasks Summary for Pipeline : ${runnableScn.pipelineBuilder.name}")
      println("******************************************************************")
      finalSession.tasks.foreach{
        case (task,metric) => println(s"Task : ${task.description} ${metric.currentStatus} after running for ${(metric.endTime - metric.startTime)/1000f} seconds")
      }
      println("******************************************************************")
      val pipelineStatus = metricsReport.count(_.currentStatus == task.Failed) match {
        case x if x > 0 => task.Failed
        case _ => Completed
      }

      //TODO : Change the status based on the failure of the tasks.
      controller ! PipelineComplete(PipelineStats(id,name,startTime.getTime,new Date().getTime,metricsReport.toArray), finalSession,pipelineStatus)


    case ExecuteWithDelay(next, delay) =>
      logger.info(s"Pausing the pipeline for the duration : ${delay.length}")
      val startTime = new Date().getTime
      val metrics = new TaskMetrics(startTime, startTime + delay.length, Completed)
      context.system.scheduler.scheduleOnce(delay, self, ExecuteNext(next, session, true, metrics))
  }
}

object PipelineManager {

  case class StartPipelineExecution()

  case class ExecuteNext(next: Task, session: Session, scheduledTask: Boolean, metrics: TaskMetrics)

  case class ExecuteWithDelay(next: Task, duration: FiniteDuration)

  case class UpdatePipelineEnvVars(envVars : Map[String,Any],eventSource: EventSource)

  case class ScenarioCompleted(session: Session)


  def props(scn: ExecutablePipeline, controller: ActorRef): Props = Props(new PipelineManager(scn, controller,scn.pipelineBuilder.name))

}
