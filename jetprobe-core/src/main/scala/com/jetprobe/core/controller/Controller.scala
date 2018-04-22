package com.jetprobe.core.controller

import java.util.Date

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{ActorSystem, OneForOneStrategy, PoisonPill, Props}
import com.jetprobe.core.task.BaseActor
import com.jetprobe.core.common.DefaultConfigs
import com.jetprobe.core.controller.ControllerCommand.{EndScenario, ScheduleTestSuites, ShutdownCmd, Start}
import com.jetprobe.core.reporter.extent.ExtentReporter
import com.jetprobe.core.reporter.{ConsoleReportWriter, HtmlReportWriter, ValidationReport}
import com.jetprobe.core.runner.PipelineManager
import com.jetprobe.core.session.{Session, UserMessage}
import com.jetprobe.core.structure.{ExecutablePipeline, Scenario}
import com.jetprobe.core.validations.{Failed, Passed, Skipped, ValidationStatus}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class Controller(hasReport: Boolean) extends BaseActor with LazyLogging {
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

    case EndScenario(session, startTime,endTime, finalStatus) =>
      val finalReport = ValidationReport(session.testName,
        session.className,
        session.validationResults.count(_.status == Failed),
        session.validationResults.count(_.status == Passed),
        session.validationResults.count(_.status == Skipped),
        startTime,
        endTime,
        finalStatus,
        session.validationResults
      )

      //Shut down the scenario Manager
      context stop (sender())

      // Add the report
      reports.+=(finalReport)
      finished = finished + 1
      if (finished == total) {
        if (session.validationResults.size > 0) {
          reporter.write(reports)

          if (hasReport) {
            logger.info(s"Generating HTML report, located at ${session.attributes.get(DefaultConfigs.htmlReportAttr).get}")
            val htmlReporter = new HtmlReportWriter(session.attributes)
            htmlReporter.write(reports.filter(p => p.detailReport.size > 0))

            ExtentReporter.build(session.attributes) match {
              case Some(extentReporter) => extentReporter.write(reports)
              case None =>
                logger.error("Unable to generate extent reports")
            }


          }


        }


        self ! ShutdownCmd
      }


    case Start(scenarios) =>
      total = scenarios.size
      logger.info(s"total test scenarios : ${total}")
      scenarios.foreach { scn =>

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
        val scheduler = context.system.actorOf(PipelineManager.props(scn, self))
        scheduler ! PipelineManager.StartPipelineExecution

      }


  }
}

object Controller {

  def props(hasReport: Boolean) = Props(new Controller(hasReport))
}

sealed trait ControllerCommand

object ControllerCommand {

  case class ScheduleTestSuites(testSuites: Seq[ExecutablePipeline]) extends ControllerCommand

  case class Start(scenarios: Seq[Scenario]) extends ControllerCommand

  case class EndScenario(session: Session, startTime: Date, endTime: Date, finalStatus: ValidationStatus) extends ControllerCommand

  case object ShutdownCmd extends ControllerCommand

}
