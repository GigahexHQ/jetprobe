package com.jetprobe.core.task

import java.util.Date

import akka.actor.ActorRef
import com.jetprobe.core.runner.PipelineManager.ScenarioCompleted
import com.jetprobe.core.session.{Session, UserMessage}

/**
  * @author Shad.
  */

case object ExitTask extends TaskType
class Exit(controller: ActorRef) extends Task {



  override def execute(session: Session): Unit = {
    logger.info("exiting the scenario")
    controller ! ScenarioCompleted(session)

  }

  override val meta: TaskMeta = TaskMeta("Exit-Action",ExitTask)
}
