package com.jetprobe.core.runner

import java.io.File
import java.net.URLClassLoader

import akka.actor.ActorSystem
import com.jetprobe.core.flow.{JobController, JobEnvironment}
import com.jetprobe.core.flow.JobController.StartJobExecution
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
object JetprobeCLI extends LazyLogging {

  var appHome: String = _
  val defaultAppVersion = "0.0.1-SNAPSHOT"

  def main(args: Array[String]): Unit = {


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
        if(result.isLeft) {
          logger.error(s"Job failed : ${result.left.get.getMessage}")
        }
      case None =>
        logger.info("Unable to parse the arguments")
        System.exit(1)
    }

  }

  def parseConfigAndRun(jarPath: Option[File], configFile: String, reportPath: String): Either[Throwable, Unit] = {
  val jobController =  jarPath match {
      case Some(jarFile) =>
        val classURL = jarFile.toURI.toURL
        val classLoader = new URLClassLoader(Array(classURL), Thread.currentThread().getContextClassLoader())
        JobController.buildFromConfig(configFile, Some(classLoader))

      case None =>
        JobController.buildFromConfig(configFile, None)

    }

    jobController match {
      case Left(throwable) => Left(throwable)
      case Right(jc) =>
        val system = ActorSystem("Jetprobe-executioner")
        JobEnvironment.system = system
        val jcActor = system.actorOf(JobController.props(jc._1,jc._2),JobController.actorName)
        jcActor ! StartJobExecution
        Right(Unit)
    }

  }

  private def getProgramVersion: String = {
    val progName = System.getProperty("prog.name", "jetprobe")
    val version = System.getProperty("prog.version", defaultAppVersion)
    val revision = System.getProperty("prog.revision", s"v-${defaultAppVersion}")
    s"$progName $version \nbuild : $revision"
  }


}
