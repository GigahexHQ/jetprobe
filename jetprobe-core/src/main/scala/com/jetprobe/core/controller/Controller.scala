package com.jetprobe.core.controller

import akka.actor.{ActorSystem, PoisonPill, Props}
import com.jetprobe.core.action.BaseActor
import com.jetprobe.core.common.DefaultConfigs
import com.jetprobe.core.controller.ControllerCommand.{ShutdownCmd, Start}
import com.jetprobe.core.reporter.{ConsoleReportWriter, HtmlReportWriter, ValidationReport}
import com.jetprobe.core.session.{Session, UserMessage}
import com.jetprobe.core.structure.Scenario
import com.jetprobe.core.validations.{Failed, Passed, Skipped}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class Controller extends BaseActor with LazyLogging {
  var total = 0
  var finished = 0
  val reports: ArrayBuffer[ValidationReport] = ArrayBuffer.empty

  override def receive: Receive = {
    case UserMessage(session, timestamp) =>
      //logger.info(s"Total time taken for ${session.testName} : ${(timestamp - session.startDate) / 1000f} sec")
      val reporter = new ConsoleReportWriter

      val status = if (session.validationResuls.count(_.status == Passed) == session.validationResuls.size) {
        Passed
      } else if (session.validationResuls.filter(_.status == Skipped).size > 0) {
        Skipped
      } else Failed

      val finalReport = ValidationReport(session.testName,
        session.className,
        session.validationResuls.count(_.status == Failed),
        session.validationResuls.count(_.status == Passed),
        session.validationResuls.count(_.status == Skipped),
        (timestamp - session.startDate) / 1000,
        status,
        session.validationResuls
      )

      // Add the
      reports.+=(finalReport)

      finished = finished + 1
      if (finished == total) {
        reporter.write(reports)
        logger.info(s"Generating HTML report, located at ${session.attributes.get(DefaultConfigs.htmlReportAttr).get}")
        val htmlReporter = new HtmlReportWriter(session.attributes)
        htmlReporter.write(reports)
        self ! ShutdownCmd
      }

    case Start(scenarios) =>
      total = scenarios.size
      logger.info(s"total test scenarios : ${total}")
      scenarios.foreach(scn => {
        val session = Session(scn.name, scn.className, attributes = scn.configAttr)
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
