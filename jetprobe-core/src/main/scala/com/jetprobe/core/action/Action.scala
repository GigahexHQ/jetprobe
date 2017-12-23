package com.jetprobe.core.action

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.runner.ScenarioManager
import com.jetprobe.core.runner.ScenarioManager.ExecuteNext
import com.jetprobe.core.session.Session
import com.typesafe.scalalogging.StrictLogging
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Shad.
  */
trait Action extends StrictLogging {

  def name: String

  def !(session: Session): Unit = execute(session)

  def execute(session: Session): Unit
}

trait ActionMessage {

  def name: String

}

trait PipedAction extends Action {

  def next: Action

}

class DelegatorAction(val name: String, actor: ActorRef) extends Action {

  override def execute(session: Session): Unit = actor ! session
}

case class ForwardedMessage(message: ActionMessage, session: Session)

class ExecutableAction(val message: ActionMessage, actorRef: ActorRef) extends Action {

  override def name: String = message.name

  override def execute(session: Session): Unit = actorRef ! ForwardedMessage(message, session)

}

class SelfExecutableAction(val name : String, val message: ActionMessage, next: Action, actorSystem: ActorSystem,scenarioManager: ActorRef)
                  (fn: (ActionMessage, Session) => Session) extends Action {

  import actorSystem.dispatcher

  implicit val timeout = Timeout(5 seconds)
  override def execute(session: Session): Unit = {
    val msg = ForwardedMessage(message, session)

    val actionActorRef = actorSystem.actorOf(SelfExecutableAction.props(next,scenarioManager,fn), name + UUID.randomUUID().toString)
    actionActorRef ! msg
    
  }


}

object SelfExecutableAction {

  def props(next : Action, scenarioManager : ActorRef, fn : (ActionMessage, Session) => Session) : Props = Props(new ActionBackedActor(next,scenarioManager) {

    override def execute(actionMessage: ActionMessage, session: Session): Session = {
      try{
        fn(actionMessage, session)
      }catch {
        case ex : Exception => logger.error(ex.getMessage)
          session
      }

    }

  })
}