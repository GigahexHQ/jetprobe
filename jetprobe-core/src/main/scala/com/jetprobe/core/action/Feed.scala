package com.jetprobe.core.action

import akka.actor.ActorRef
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
class Feed(receiver : ActorRef, next : Action) extends Action{

  override val name: String = "feed"

  override def execute(session: Session): Unit = receiver ! FeedMessage(session,next)

}
