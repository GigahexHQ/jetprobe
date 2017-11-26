package com.jetprobe.core.action

import akka.actor.{ActorSystem, Props}
import com.jetprobe.core.session.Session

import scala.concurrent.duration._

/**
  * @author Shad.
  */
class Pause(duration : FiniteDuration, system : ActorSystem, next : Action) extends Action{
  override def name: String = "pause for "
  import system.dispatcher

  override def execute(session: Session): Unit = {
    val delegator = system.actorOf(Props(new ActionDelegatorActor(next,session)))
    system.scheduler.scheduleOnce(duration,delegator,ExecuteCommand)
  }
}
