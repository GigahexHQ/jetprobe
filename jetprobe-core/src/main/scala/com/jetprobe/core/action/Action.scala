package com.jetprobe.core.action

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.session.Session
import com.typesafe.scalalogging.StrictLogging

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

case class ForwardedMessage(message: ActionMessage, session: Session)

class SelfExecutableAction(val name : String, val message: ActionMessage, next: Action, actorSystem: ActorSystem,scenarioManager: ActorRef)
                  (fn: (ActionMessage, Session) => Session) extends Action {


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