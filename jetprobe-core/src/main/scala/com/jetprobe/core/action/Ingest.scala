package com.jetprobe.core.action

import akka.actor.ActorRef
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
class Ingest(sinkActor : ActorRef, next : Action) extends Action{

  override val name: String = "feed"

  override def execute(session: Session): Unit = sinkActor ! FeedMessage(session,next)

}
