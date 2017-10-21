package com.jetprobe.core.action

import java.util.Date

import akka.actor.ActorRef
import com.jetprobe.core.session.{Session, UserMessage}

/**
  * @author Shad.
  */
class Exit(controller: ActorRef) extends Action {

  override def name: String = "jetprobe-exit"
  override def execute(session: Session): Unit = {

    controller ! UserMessage(session, new Date().getTime)

  }
}
