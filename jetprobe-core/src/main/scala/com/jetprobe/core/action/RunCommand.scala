package com.jetprobe.core.action

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
class RunCommand(command : String, at : String,next: Action, actorSystem: ActorSystem, scenarioController: ActorRef) extends Action {

  override def name: String = ???

  override def execute(session: Session): Unit = ???
}
