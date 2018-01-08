package com.jetprobe.core.action

import java.util.Date
import akka.actor.ActorRef
import com.jetprobe.core.runner.ScenarioManager.ExecuteNext
import com.jetprobe.core.session.Session
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
abstract class ActionActor extends BaseActor {

  def next: Action

  override def receive: Receive = {

    case ForwardedMessage(message,session) =>
      execute(message,session)
      context stop self
  }

  def execute(actionMessage: ActionMessage,session: Session) : Unit

  /**
    * Makes sure that in case of an actor crash, the Session is not lost but passed to the next Action.
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

abstract class ActionBackedActor(next : Action,controller : ActorRef) extends BaseActor {

  override def receive: Receive = {

    case ForwardedMessage(message,session) =>

      val startTime = new Date().getTime

      val updatedSession = Try{
        execute(message,session)
      } match {
        case Success(sess) =>
          val metrics = new ActionMetrics(message.name,startTime,new Date().getTime,Failed)
          controller ! ExecuteNext(next,sess,false,metrics)
        case Failure(ex) =>
          logger.error(ex.getMessage)
          val metrics = new ActionMetrics(message.name,startTime,new Date().getTime,Failed)
          controller ! ExecuteNext(next,session,false,metrics)
      }




    case _ =>
      logger.error(s"Unsupported message for action ${next.name}")
  }

  def execute(actionMessage: ActionMessage,session: Session) : Session


  /**
    * Makes sure that in case of an actor crash, the Session is not lost but passed to the next Action.
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

class ActionMetrics(val name : String, val startTime : Long, val endTime : Long, state : ActionState)

trait ActionState
case object Successful extends ActionState
case object Failed extends ActionState