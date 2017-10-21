package com.jetprobe.core.controller

import akka.actor.{ActorSystem, PoisonPill, Props}
import com.jetprobe.core.action.BaseActor
import com.jetprobe.core.controller.ControllerCommand.{ShutdownCmd, Start}
import com.jetprobe.core.reporter.ConsoleReportWriter
import com.jetprobe.core.session.{Session, UserMessage}
import com.jetprobe.core.structure.Scenario
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
class Controller extends BaseActor with LazyLogging {
  var total = 0
  var finished = 0

  override def receive: Receive = {
    case UserMessage(session, timestamp) =>
      //logger.info(s"Total time taken for ${session.testName} : ${(timestamp - session.startDate) / 1000f} sec")
      val reporter = new ConsoleReportWriter
      reporter.report(session.testName,session.className,session.validationResuls)
      finished = finished + 1
      if(finished == total){
        self ! ShutdownCmd
      }

    case Start(scenarios) =>
      total = scenarios.size
      logger.info(s"total test scenarios : ${total}")
      scenarios.foreach(scn => {
        val session = Session(scn.name,scn.className)
        logger.info(s"Starting the session : ${scn.name}")
        scn.entry ! session
      })


    case ShutdownCmd =>
      val allActors = context.actorSelection("/user/*").forward(PoisonPill)
      logger.debug("Shutting down the actor system")
      context stop self
      context.system.terminate().onComplete({
        case _ =>
          logger.debug("Actor system shutdown")
          System.exit(0)
      })



  }
}

object Controller {

  def props = Props(new Controller)
}

sealed trait ControllerCommand

object ControllerCommand {

  case class Start(scenarios: Seq[Scenario]) extends ControllerCommand

  case object ShutdownCmd extends ControllerCommand

}
