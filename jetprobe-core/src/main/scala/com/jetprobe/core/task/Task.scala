package com.jetprobe.core.task

import java.util.{Date, UUID}

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.session.Session
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.duration._

/**
  * @author Shad.
  */

/**
  * Defines the task that gets executed
  */
trait Task extends StrictLogging {

  val meta : TaskMeta

  def !(session: Session): Unit = execute(session)

  def execute(session: Session): Unit
}

case class TaskMeta(description : String, taskType : TaskType, maxDuration : FiniteDuration = 2.hours, retries : Int = 0) {

  val tId  :String = description.toLowerCase.replaceAll(" ","-") + "-" + UUID.randomUUID().toString
}

trait TaskMessage {

  def name: String

}

trait TaskType

sealed trait RunStatus
case object NotStarted extends RunStatus {

}
case object Running extends RunStatus
case object Completed extends RunStatus {
  override def toString: String = "Completed"
}
case object Failed extends RunStatus
case object Skipped extends RunStatus

case class TaskMetrics(startTime : Long, endTime : Long, currentStatus : RunStatus)


case class ForwardedMessage(message: TaskMessage, session: Session, task : TaskMeta)

class SelfExecutableTask(val meta : TaskMeta, val message: TaskMessage, next: Task, actorSystem: ActorSystem,scenarioManager: ActorRef)
                  (fn: (TaskMessage, Session) => Session) extends Task {

  override def execute(session: Session): Unit = {
    val msg = ForwardedMessage(message, session,meta)

    val taskActorRef = actorSystem.actorOf(SelfExecutableTask.props(next,scenarioManager,fn), meta.tId)
    taskActorRef ! msg
    
  }

}

object SelfExecutableTask {

  def props(next : Task, scenarioManager : ActorRef, fn : (TaskMessage, Session) => Session) : Props = Props(new TaskBackedActor(next,scenarioManager) {

    override def execute(taskMessage: TaskMessage, session: Session): Session = {
      //try{
        fn(taskMessage, session)
      /*}catch {
        case ex : Exception => logger.error(ex.getMessage)
          session
      }*/

    }

  })
}