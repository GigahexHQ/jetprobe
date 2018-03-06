package com.jetprobe.core.reporter.extent

import com.aventstack.extentreports.ExtentReports
import com.aventstack.extentreports.reporter.{ExtentHtmlReporter, KlovReporter}
import com.jetprobe.core.reporter.{ResultReporter, ValidationReport}
import com.jetprobe.core.validations.{Failed, Passed, Skipped}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class ExtentReporter(fileName: String,project : String, reportName : String, mongoHost : String) extends ResultReporter {

  val htmlReporter = new ExtentHtmlReporter(fileName)
  val klov = new KlovReporter(project,reportName)
  klov.initMongoDbConnection(mongoHost)


  override def write(reports: Seq[ValidationReport]): Unit = {
    val extent = new ExtentReports()
    extent.attachReporter(htmlReporter,klov)

    reports.filter(p => p.detailReport.nonEmpty).foreach{ report =>

      val testSuite = extent.createTest(report.suite)
      testSuite.getModel.setStartTime(report.startTime)
      testSuite.getModel.setEndTime(report.endTime)

      if(report.finalSatus == Passed){
        testSuite.pass("Test Passed")
      } else if(report.finalSatus == Skipped){
        testSuite.skip(s"${report.skippedCount} tests skipped")
      } else {
        testSuite.fail(s"${report.failedCount} tests failed")
      }

      report.detailReport.foreach{ result =>

        val test = testSuite.createNode(result.testName)
        result.status match {
          case Passed => test.pass(s"${result.testName} passed")
          case Skipped => test.skip(s"${result.message}")
          case Failed => test.fail(s"${result.message}")
        }

      }

    }

    extent.flush()
    klov.flush()
  }

}

object ExtentReporter {

  val propProjectName = "reporter.extent.project"
  val propFileName = "reporter.extent.fileName"
  val propReportName = "reporter.extent.reportName"
  val propMongoHost = "reporter.extent.mongoHost"

  def build(config : Map[String,Any]) : Option[ExtentReporter] = {
    val reporter = Try{
      val projectName = config(propProjectName).toString
      val fileName = config(propFileName).toString
      val mongoHost = config(propMongoHost).toString
      val reportName = config(propReportName).toString
      (projectName,fileName,mongoHost,reportName)
    }

    reporter match {
      case Success((projectName,fileName,mongoHost,reportName)) => Some(new ExtentReporter(fileName,projectName,reportName,mongoHost))
      case Failure(exception) =>
        println(s"Failed while building extent reporter : ${exception.getMessage}")
        None
    }
  }
}