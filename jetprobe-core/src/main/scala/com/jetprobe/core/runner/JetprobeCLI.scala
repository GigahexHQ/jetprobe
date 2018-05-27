package com.jetprobe.core.runner

import java.io.File
import java.net.URLClassLoader

import akka.actor.ActorSystem
import com.jetprobe.core.flow.{JobController, JobEnvironment}
import com.jetprobe.core.flow.JobController.StartJobExecution
import com.typesafe.scalalogging.LazyLogging
import akka.pattern._
import akka.util.Timeout
import com.jetprobe.core.task.{Completed, RunStatus}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.util.Success
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Shad.
  */
object JetprobeCLI extends LazyLogging {

  var appHome: String = _
  val defaultAppVersion = "0.0.1-SNAPSHOT"

  def main(args: Array[String]): Unit = {
    val result = run(args)
    logger.info(s"Job finished with success : ${result}")
  }

  def parseConfigAndRun(jarPath: Option[File], configFile: String, reportPath: Option[String]): Either[Throwable, Boolean] = {
    val jobController = jarPath match {
      case Some(jarFile) =>
        val classURL = jarFile.toURI.toURL
        val classLoader = new URLClassLoader(Array(classURL), Thread.currentThread().getContextClassLoader())
        JobController.buildFromConfig(configFile, Some(classLoader))

      case None =>
        JobController.buildFromConfig(configFile, Some(Thread.currentThread().getContextClassLoader()))

    }

    jobController match {
      case Left(throwable) => Left(throwable)
      case Right(jc) =>

        val system = ActorSystem("Jetprobe-executioner", ConfigFactory.load(getClass.getClassLoader))
        JobEnvironment.system = system
        val jcActor = system.actorOf(JobController.props(jc._1, jc._2, jc._3, true, reportPath), JobController.actorName)
        implicit val timeout = Timeout(10000.seconds)
        val returnStatus = jcActor ? StartJobExecution
        val result = returnStatus.mapTo[RunStatus]
        val futureRes = result map {
          case Completed => true
          case _ => false
        }

        Right(Await.result(futureRes, 1000.seconds))
    }

  }

  private def getProgramVersion: String = {
    val progName = System.getProperty("prog.name", "jetprobe")
    val version = System.getProperty("prog.version", defaultAppVersion)
    val revision = System.getProperty("prog.revision", s"v-${defaultAppVersion}")
    s"$progName $version \nbuild : $revision"
  }

  def run(args: Array[String]): Boolean = {

    appHome = System.getProperty("prog.home")

    //TODO Better approach to show the version
    if (args.length == 1 && (args(0) == "-v" || args(0) == "--version")) {
      println(getProgramVersion)
      System.exit(0)
    }
    val parsedConfig = CmdConfig.parser.parse(args, CmdConfig())
    parsedConfig match {
      case Some(conf) =>
        val result = parseConfigAndRun(conf.jobJarPath, conf.configFile, conf.reportPath)
        if (result.isLeft) {
          logger.error(s"Job failed : ${result.left.get.getMessage}")
          false
        } else
          result.right.get
      case None =>
        logger.info("Unable to parse the arguments")
        false
    }
  }


}
