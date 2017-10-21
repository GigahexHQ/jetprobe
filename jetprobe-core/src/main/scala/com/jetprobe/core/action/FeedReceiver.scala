package com.jetprobe.core.action

import akka.actor.Props
import com.jetprobe.core.generator.Generator
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
object FeedReceiver {
  def props(feeder: Generator) = Props(new FeedReceiver(feeder))
}

class FeedReceiver(val feeder: Generator) extends BaseActor {

  override def receive: Receive = {
    case FeedMessage(session, next) =>
      val newSession = session.copy(records = feeder)
      next ! newSession

  }
}

case class FeedMessage(session: Session, next: Action)
