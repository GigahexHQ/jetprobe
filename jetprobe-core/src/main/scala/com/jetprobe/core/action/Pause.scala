package com.jetprobe.core.action

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.runner.ScenarioManager.{ExecuteNext, ExecuteWithDelay}
import com.jetprobe.core.session.Session

import scala.concurrent.duration._

/**
  * @author Shad.
  */
class Pause(duration : FiniteDuration, next : Action,actorSystem : ActorSystem,scenarioController : ActorRef) extends Action{

  import actorSystem.dispatcher

  override def name: String = "pause for "

  override def execute(session: Session): Unit = {
    logger.info(s"Pausing for the duration ${duration.length}")
    actorSystem.scheduler.scheduleOnce(duration,scenarioController,ExecuteNext(next,session,true))
    //scenarioController ! ExecuteWithDelay(next,duration)
  }
}
