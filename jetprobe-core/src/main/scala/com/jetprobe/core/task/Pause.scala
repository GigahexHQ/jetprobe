package com.jetprobe.core.task

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.runner.PipelineManager.{ExecuteNext, ExecuteWithDelay}
import com.jetprobe.core.session.Session

import scala.concurrent.duration._

/**
  * @author Shad.
  */

case object PauseTask extends TaskType

class Pause(val description : String, duration: FiniteDuration, next: Task, actorSystem: ActorSystem, scenarioController: ActorRef) extends Task {

  import actorSystem.dispatcher


  val taskMeta = TaskMeta(description,PauseTask)

  override def execute(session: Session): Unit = {
    logger.info(s"Pausing for the duration ${duration.length}")

    val startTime = new Date().getTime
    val metrics = new TaskMetrics( startTime, startTime + duration.length, Completed)
    actorSystem.scheduler.scheduleOnce(duration, scenarioController, ExecuteNext(next, session, true, metrics))

  }

  override val meta: TaskMeta = TaskMeta(description,PauseTask)
}
