package com.jetprobe.core.task

import java.util.Date
import akka.actor.ActorRef
import com.jetprobe.core.runner.PipelineManager.ExecuteNext
import com.jetprobe.core.session.Session
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
abstract class TaskActor extends BaseActor {

  def next: Task

  override def receive: Receive = {

    case ForwardedMessage(message,session,meta) =>
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
      case ForwardedMessage(msg,session,meta) =>
        logger.error(s"'${self.path.name}' crashed while executing ${msg.name}, with cause : ${reason.getMessage}")
        next.execute(session)

    }
}

abstract class TaskBackedActor(next : Task,controller : ActorRef) extends BaseActor {

  override def receive: Receive = {

    case ForwardedMessage(message,session,task) =>

      val startTime = new Date().getTime
      val shouldBeSkipped = session.tasks.exists(_._2.currentStatus == Failed) && session.exitOnFailure

      val updatedSession = Try{
        if(shouldBeSkipped){
          session
        } else execute(message,session)
      } match {
        case Success(sess) =>
          val taskStatus = if(shouldBeSkipped) Skipped else Completed
          val metrics = new TaskMetrics(startTime,new Date().getTime,taskStatus)
          controller ! ExecuteNext(next,sess.copy(currentStatus = taskStatus,tasks = sess.tasks + (task -> metrics)),false,metrics)
        case Failure(ex) =>
          logger.error(s"Exception occurred while executing the task : ${task.description} : ${ex.getMessage}")

          val metrics = new TaskMetrics(startTime,new Date().getTime,Failed)

          controller ! ExecuteNext(next,session.copy(currentStatus = Failed,tasks = session.tasks + (task -> metrics)),false,metrics)
      }

    case _ =>
      logger.error(s"Unsupported message for task ${next.meta.description}")
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
      case ForwardedMessage(msg,session,meta) =>
        logger.error(s"'${self.path.name}' crashed while executing ${msg.name}, with cause : ${reason.getMessage}")
        next.execute(session)

    }
}

/*
class TaskMetrics(val name : String, val startTime : Long, val endTime : Long, state : TaskState)

trait TaskState
case object Successful extends TaskState
case object Failed extends TaskState*/
