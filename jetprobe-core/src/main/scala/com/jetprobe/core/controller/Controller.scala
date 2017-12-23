package com.jetprobe.core.controller

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{ActorSystem, OneForOneStrategy, PoisonPill, Props}
import com.jetprobe.core.action.BaseActor
import com.jetprobe.core.common.DefaultConfigs
import com.jetprobe.core.controller.ControllerCommand.{EndScenario, ScheduleTestSuites, ShutdownCmd, Start}
import com.jetprobe.core.reporter.{ConsoleReportWriter, HtmlReportWriter, ValidationReport}
import com.jetprobe.core.runner.ScenarioManager
import com.jetprobe.core.session.{Session, UserMessage}
import com.jetprobe.core.structure.{ExecutableScenario, Scenario}
import com.jetprobe.core.validations.{Failed, Passed, Skipped, ValidationStatus}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class Controller(hasReport : Boolean) extends BaseActor with LazyLogging {
  var total = 0
  var finished = 0
  val reports: ArrayBuffer[ValidationReport] = ArrayBuffer.empty
  val reporter = new ConsoleReportWriter
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
      case _: ArithmeticException => Resume
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Stop
    }

  override def receive: Receive = {

    case EndScenario(session,timeTaken : Double,finalStatus) =>
      val finalReport = ValidationReport(session.testName,
        session.className,
        session.validationResuls.count(_.status == Failed),
        session.validationResuls.count(_.status == Passed),
        session.validationResuls.count(_.status == Skipped),
        timeTaken,
        finalStatus,
        session.validationResuls
      )

      //Shut down the scenario Manager
      context stop(sender())

      // Add the report
      reports.+=(finalReport)
      finished = finished + 1
      if (finished == total) {
        reporter.write(reports)


        if(hasReport){
          logger.info(s"Generating HTML report, located at ${session.attributes.get(DefaultConfigs.htmlReportAttr).get}")
          val htmlReporter = new HtmlReportWriter(session.attributes)
          htmlReporter.write(reports.filter(p => p.detailReport.size > 0))
        }

        self ! ShutdownCmd
      }


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


        if(hasReport){
          logger.info(s"Generating HTML report, located at ${session.attributes.get(DefaultConfigs.htmlReportAttr).get}")
          val htmlReporter = new HtmlReportWriter(session.attributes)
          htmlReporter.write(reports.filter(p => p.detailReport.size > 0))
        }

        self ! ShutdownCmd
      }

    case Start(scenarios) =>
      total = scenarios.size
      logger.info(s"total test scenarios : ${total}")
      scenarios.foreach{ scn =>

      }
      scenarios.foreach(scn => {
        val session = Session(scn.name, scn.className, attributes = scn.configAttr)
        logger.info(s"Starting the session : ${scn.name}")
        scn.entry ! session
      })


    case ShutdownCmd =>
      //val allActors = context.actorSelection("/user/*").forward(PoisonPill)
      //logger.debug("Shutting down the actor system")
      context stop self
      context.system.terminate().onComplete({
        case _ =>
          logger.debug("Actor system shutdown")
          System.exit(0)
      })

    case ScheduleTestSuites(scns) =>
      total = scns.size
      logger.info(s"Total number of test suites : ${scns.length}")
      scns.foreach { scn =>
        val scheduler = context.system.actorOf(ScenarioManager.props(scn,self))
        scheduler ! ScenarioManager.StartScenario

      }


  }
}

object Controller {

  def props(hasReport : Boolean) = Props(new Controller(hasReport))
}

sealed trait ControllerCommand

object ControllerCommand {

  case class ScheduleTestSuites(testSuites : Seq[ExecutableScenario]) extends ControllerCommand

  case class Start(scenarios: Seq[Scenario]) extends ControllerCommand

  case class EndScenario(session: Session,timeTaken : Double, finalStatus : ValidationStatus) extends ControllerCommand

  case object ShutdownCmd extends ControllerCommand

}
