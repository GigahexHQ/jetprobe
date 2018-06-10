package com.jetprobe.core.reporter.extent

import java.util.Date

import com.aventstack.extentreports.ExtentReports
import com.aventstack.extentreports.reporter.{ExtentHtmlReporter, KlovReporter}
import com.jetprobe.core.reporter.{ResultReporter, ValidationReport}
import com.jetprobe.core.validations.{Failed, Passed, Skipped}
import com.typesafe.scalalogging.LazyLogging
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class ExtentReporter(fileName: String,project : String, reportName : String, mongoHost : Option[String])
  extends ResultReporter[ExtentReports] {

  val htmlReporter = new ExtentHtmlReporter(fileName)
  lazy val klov : Option[KlovReporter] = {
    mongoHost.map(host => new KlovReporter(project,reportName))

  }



  override def write(reports: Seq[ValidationReport]): ExtentReports = {
    val extent = new ExtentReports()
    klov match {
      case Some(kv) =>
        try {
          kv.initMongoDbConnection(mongoHost.get)
          extent.attachReporter(htmlReporter,kv)
        } catch {
          case e : Exception =>
            logger.warn(s"Unable to connect with mongo db : ${e.getMessage}")
            //e.printStackTrace()
            extent.attachReporter(htmlReporter)

        }

      case None => extent.attachReporter(htmlReporter)

    }


    reports.filter(p => p.detailReport.nonEmpty).foreach{ report =>

      val testSuite = extent.createTest(report.suite)
      val (testSuiteStart,testSuiteEnd) = report.detailReport.foldLeft((Long.MaxValue,Long.MinValue)) {
        case ((st,end),result) =>
          val lowestStartTime = if(st > result.startTime) result.startTime else st
          val maxEndTime = if(end > result.endTime) end else result.endTime
          (lowestStartTime,maxEndTime)
      }
      testSuite.getModel.setStartTime(new Date(testSuiteStart))
      testSuite.getModel.setEndTime(new Date(testSuiteEnd))

      if(report.finalSatus == Passed){
        testSuite.pass("Test Passed")
      } else if(report.finalSatus == Skipped){
        testSuite.skip(s"${report.skippedCount} tests skipped")
      } else {
        testSuite.fail(s"${report.failedCount} tests failed")
      }

      report.detailReport.foreach{ result =>

        val test = testSuite.createNode(result.testName)
        test.getModel.setStartTime(new Date(result.startTime))
        test.getModel.setEndTime(new Date(result.endTime))
        result.status match {
          case Passed => test.pass(s"${result.testName} passed")
          case Skipped => test.skip(s"${result.message}")
          case Failed => test.fail(s"${result.message}")
          case _ => test.error(s"${result.message}")
        }

      }

    }

    extent.flush()
    if(klov.nonEmpty){
      klov.get.flush()
    }

    extent
  }



}

object ExtentReporter extends LogSupport {


  val propProjectName = "reporter.extent.project"
  val propFileName = "reporter.extent.fileName"
  val propReportName = "reporter.extent.reportName"
  val propMongoHost = "reporter.extent.mongoHost"

  def build(scenarioName : String, config : Map[String,Any]) : Option[ExtentReporter] = {
    val reporter = Try{
      val projectName = config(propProjectName).toString
      val fileName = config(propFileName).toString
      val mongoHost = config.get(propMongoHost).map(_.asInstanceOf[String])
      val reportName = config(propReportName).toString
      (projectName,fileName,mongoHost,reportName)
    }

    reporter match {
      case Success((projectName,fileName,mongoHost,reportName)) => Some(new ExtentReporter(fileName,projectName,reportName,mongoHost))
      case Failure(exception) =>
        warn(s"Extent reporter missing fields : ${exception.getMessage}")
        None
    }
  }
}