package com.jetprobe.core.action

import akka.actor.ActorRef
import com.jetprobe.core.session.Session


/**
  * @author Shad.
  */
class Validate(validator: ActorRef, next: Action) extends Action {

  override def name: String = "validate"

  override def execute(session: Session): Unit = validator ! FeedMessage(session, next)
}

