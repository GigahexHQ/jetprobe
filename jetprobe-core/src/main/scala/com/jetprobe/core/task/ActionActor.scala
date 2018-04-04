package com.jetprobe.core.task

import java.util.Date
import akka.actor.ActorRef
import com.jetprobe.core.runner.ScenarioManager.ExecuteNext
import com.jetprobe.core.session.Session
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
abstract class TaskActor extends BaseActor {

  def next: Task

  override def receive: Receive = {

    case ForwardedMessage(message,session) =>
      execute(message,session)
      context stop self
  }

  def execute(taskMessage: TaskMessage,session: Session) : Unit

  /**
    * Makes sure that in case of an actor crash, the Session is not lost but passed to the next Task.
    */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit =
    message.foreach {
      case session: Session =>
        logger.error(s"'${self.path.name}' crashed on session $session, forwarding to the next one", reason)
        next.execute(session)
      case ForwardedMessage(msg,session) =>
        logger.error(s"'${self.path.name}' crashed while executing ${msg.name}, with cause : ${reason.getMessage}")
        next.execute(session)

    }
}

abstract class TaskBackedActor(next : Task,controller : ActorRef) extends BaseActor {

  override def receive: Receive = {

    case ForwardedMessage(message,session) =>

      val startTime = new Date().getTime

      val updatedSession = Try{
        execute(message,session)
      } match {
        case Success(sess) =>
          val metrics = new TaskMetrics(message.name,startTime,new Date().getTime,Failed)
          controller ! ExecuteNext(next,sess,false,metrics)
        case Failure(ex) =>
          logger.error(ex.getMessage)
          val metrics = new TaskMetrics(message.name,startTime,new Date().getTime,Failed)
          controller ! ExecuteNext(next,session,false,metrics)
      }




    case _ =>
      logger.error(s"Unsupported message for task ${next.name}")
  }

  def execute(taskMessage: TaskMessage,session: Session) : Session


  /**
    * Makes sure that in case of an actor crash, the Session is not lost but passed to the next Task.
    */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit =
    message.foreach {
      case session: Session =>
        logger.error(s"'${self.path.name}' crashed on session $session, forwarding to the next one", reason)
        next.execute(session)
      case ForwardedMessage(msg,session) =>
        logger.error(s"'${self.path.name}' crashed while executing ${msg.name}, with cause : ${reason.getMessage}")
        next.execute(session)

    }
}

class TaskMetrics(val name : String, val startTime : Long, val endTime : Long, state : TaskState)

trait TaskState
case object Successful extends TaskState
case object Failed extends TaskState