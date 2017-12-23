package com.jetprobe.core.action

import akka.actor.{ActorRef, PoisonPill}
import com.jetprobe.core.runner.ScenarioManager.ExecuteNext
import com.jetprobe.core.session.Session

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
abstract class ActionActor extends BaseActor {

  def next: Action

  override def receive: Receive = {
    /*case session: Session =>
      execute(session)
      context stop self*/

    case ForwardedMessage(message,session) =>
      execute(message,session)
      context stop self
  }

  def execute(actionMessage: ActionMessage,session: Session) : Unit

  //def execute(session: Session): Unit = ???

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

      logger.info("Executing the message of the current actor")

      val updatedSession = Try{
        execute(message,session)
      } match {
        case Success(s) => s
        case Failure(ex) =>
          logger.error(ex.getMessage)
          session
      }

      controller ! ExecuteNext(next,updatedSession,false)


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