package com.jetprobe.core.flow

import java.awt.Desktop
import java.io.File
import java.net.URI

import akka.actor.{ActorRef, Props}
import cats.syntax.either._
import com.jetprobe.core.flow.JobController._
import com.jetprobe.core.flow.ScenarioExecutor._
import com.jetprobe.core.generator.ActorNameGenerator
import com.jetprobe.core.reporter.{ConsoleReportWriter, HtmlReportWriter, ValidationReport}
import com.jetprobe.core.reporter.extent.ExtentReporter
import com.jetprobe.core.structure.PipelineBuilder
import com.jetprobe.core.task._
import com.jetprobe.core.validations
import io.circe.yaml.parser
import com.jetprobe.core.flow.JobDescriptors._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * @author Shad.
  */

class JobController(project : String, scenarios: mutable.Queue[ScenarioMeta],
                    classLoader: Option[ClassLoader] = None,
                    shutDownWhenDone : Boolean = false,
                    reportOutPath : Option[String] = None) extends BaseActor {

  private val jpHome = "JP_HOME"
  private val jpProperty = "jp.home"
  var failureCount = 0

  var originalSender : ActorRef = _
  val scenarioMetrics : mutable.Map[String,ArrayBuffer[PipelineStats]] = mutable.Map.empty

  val clsLoader = classLoader match {
    case Some(x) => x
    case None => Thread.currentThread().getContextClassLoader
  }

  var envVars: Map[String, Any] = {
    if(System.getenv(jpHome)!= null){
      Map(jpProperty -> System.getenv(jpHome))
    }
    else {
      warn(s"Environment variable ${jpHome} is not set.")
      Map.empty
    }
  }

  override def receive: Receive = {

    case StartJobExecution =>
      originalSender = sender()
      val scenario = scenarios.dequeue()
      val queueScns = mutable.Queue(scenario.pipelines.map(p => PipelineBuilder(p.name, p.className)))
      val scnExecutor = ScenarioExecutor.buildScenario(scenario.pipelines, clsLoader).map(scn => ScenarioExecutor.props(scenario.name, scn, self))
      scnExecutor match {
        case Left(throwable) => self ! StopJobExecution(Failed, throwable.getMessage)
        case Right(props) =>
          val scnActor = context.actorOf(props, ActorNameGenerator.getName(scenario.name))
          scnActor ! StartScenarioExecution(scenario.params)
      }

    case ScenarioCompleted(name, metrics, status) =>
      logger.info(s"Job completed ")
      scenarioMetrics += (name -> metrics)

      //Display the validation report if present.
      val validationReports = metrics.filter(_.validationResults.size > 0) map { stats =>

        val failedCount = stats.validationResults.count(_.status == validations.Failed)
        val passedCount = stats.validationResults.count(_.status == validations.Passed)
        val skippedCount = stats.validationResults.count(_.status == validations.Skipped)
        val finalStatus = {
          if (passedCount == stats.validationResults.size) validations.Passed
          else if (failedCount > 0) {
            failureCount = failureCount + failedCount
            validations.Failed

          }
          else validations.Skipped
        }
        ValidationReport(stats.name, stats.className, failedCount, passedCount, skippedCount, finalStatus, stats.validationResults)

      }

      if(validationReports.nonEmpty) {
        val klovReporter = envVars.get(ExtentReporter.propReportName).flatMap(_ => ExtentReporter.build(name,envVars))
        klovReporter match {
          case Some(klv) => klv.write(validationReports)
          case None => warn("Klov Reporter config not found.")
        }

        new ConsoleReportWriter().write(validationReports)

        //HTML Report
        for {
          reportPath <- reportOutPath
          htmlReport <- envVars.get(jpProperty).map( x => new HtmlReportWriter(envVars,x.toString,reportPath,project))
        }yield {
          logger.info("Generating html reports")
          val reportFile = htmlReport.write(validationReports)
          if(Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            Desktop.getDesktop().browse(reportFile.toURI)
          }

        }

      }


      self ! StopJobExecution(status, "Success")


    case StopJobExecution(status, message) =>
      status match {
        case Completed => info("All tests pipelines completed.")
        case Skipped => warn(s"Some test pipelines were skipped with message : ${message}")
        case Failed => error(s"Tests Pipeline failed with exception : ${message}")
      }

      stopChildren
      originalSender ! status
      context stop self
      if(shutDownWhenDone){
        context.system.terminate().onComplete({
          case _ =>
            info("Actor system shutdown")
          //System.exit(0)
        })
      }




    case UpdateJobEnvVars(vars, eventSource) =>
      envVars = envVars ++ vars
      eventSource match {
        case FromUser => context.children.foreach(actor => actor ! UpdateScnEnvVariables(vars, FromJobController))
        case _ => logger.debug("Received update for vars")
      }

    case GetCurrentEnvVars =>
      sender() ! envVars

  }


}

object JobController {

  val actorName = "JobController"

  type JobParams = (String, mutable.Queue[ScenarioMeta], Option[ClassLoader])

  def props(project : String, scenarios: mutable.Queue[ScenarioMeta], classLoader: Option[ClassLoader]): Props =
    Props(new JobController(project,scenarios, classLoader))

  def props(project : String, scenarios: mutable.Queue[ScenarioMeta], classLoader: Option[ClassLoader],
            shutDownWhenDone : Boolean,
            reportPath : Option[String]): Props =
    Props(new JobController(project,scenarios, classLoader,shutDownWhenDone,reportPath))

  case object StartJobExecution

  case class UpdateJobEnvVars(vars: Map[String, Any], eventSource: EventSource)

  case object GetCurrentEnvVars

  case class ScenarioCompleted(name: String, metrics: ArrayBuffer[PipelineStats], status: RunStatus)

  case class StopJobExecution(status: RunStatus, message: String = "Completed Job")


  def buildFromConfig(path: String, optClassLoader: Option[ClassLoader]): Either[Throwable, JobParams] = {
    val externalFile = new File(path)
    val yamlString: Option[String] = externalFile match {
      case f if (f.isFile) => Some(Source.fromFile(f).getLines().toSeq.mkString("\n"))
      case f if (!f.isFile) =>
        //check for resource path
        val classLoader = optClassLoader.getOrElse(getClass.getClassLoader)
        val fileStream = classLoader.getResourceAsStream(path)
        fileStream match {
          case fs if (fs == null) => None
          case _ =>
            val lines = Source.fromInputStream(fileStream).getLines().toSeq.mkString("\n")
            Some(lines)
        }

      case _ => None
    }

    yamlString match {
      case Some(yaml) =>
        parser.parse(yaml)
          .leftMap(err => err.underlying)
          .flatMap { js =>
            val extractScn = js.as[ScenarioMeta]
            extractScn.map(v => (v.project,mutable.Queue(v), optClassLoader))

          }

      case None =>
        Left(new Exception(s"Unable to parse file at ${path}"))

    }

  }

}
