package com.jetprobe.core.runner

import java.io.File
import java.net.{URL, URLClassLoader}
import java.util.function.Consumer
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.util
import java.util.zip.ZipInputStream

import akka.actor.ActorSystem
import com.jetprobe.core.TestScenario
import com.jetprobe.core.common.{ConfigReader, DefaultConfigs, Version}
import com.jetprobe.core.structure.ExecutableScenario
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
object TestRunner extends LazyLogging with Version {

  var appHome: String = _

  def main(args: Array[String]): Unit = {

    if (System.getenv("JETPROBE_HOME") == null) {
      println("JETPROBE_HOME variable must be set before running the application")
      System.exit(1)
    }
    appHome = System.getenv("JETPROBE_HOME")

    //TODO Better approach to show the version
    if (args.length == 1 && (args(0) == "-v" || args(0) == "--version")) {
      println(currentVersion)
      System.exit(0)
    }
    val parsedConfig = CmdConfig.parser.parse(args, CmdConfig())
    parsedConfig match {
      case Some(conf) => extractAndRun(conf.testableJar.getAbsolutePath, conf.configFile, conf.reportPath)
      case None =>
        logger.info("Unable to parse the arguments")
        System.exit(1)
    }

  }


  def extractAndRun(jarPath: String, configFile: Option[File], reportPath: String): Unit = {
    val classNames = new util.ArrayList[String]
    val zip = new ZipInputStream(new FileInputStream(jarPath))
    var entry = zip.getNextEntry
    while ( {
      entry != null
    }) {
      if (!entry.isDirectory && entry.getName.endsWith(".class")) {
        val className = entry.getName.replace('/', '.')
        classNames.add(className.substring(0, className.length - ".class".length))
      }

      entry = zip.getNextEntry
    }

    executeTests(jarPath, classNames, configFile, reportPath)

  }

  def executeTests(jarPath: String, classNames: util.ArrayList[String], configFile: Option[File], reportPath: String): Unit = {
    val classURL = new File(jarPath).toURI.toURL
    val classLoader = new URLClassLoader(Array(classURL), Thread.currentThread().getContextClassLoader())

    implicit val system = ActorSystem("Jetprobe-system")
    val runner = Runner()
    val scenarios = ArrayBuffer.empty[ExecutableScenario]

    classNames.forEach((csName: String) => {
      val testBuilder = classLoader.loadClass(csName)
      println(s"loading class $csName")

      val isAbstract = Modifier.isAbstract(testBuilder.getModifiers)

      if (!isAbstract) {
        val t = testBuilder.newInstance()

        if (t.isInstanceOf[TestScenario]) {
          val m = testBuilder.getDeclaredMethod("buildScenario")
          m.setAccessible(true)
          val result = m.invoke(t).asInstanceOf[ExecutableScenario]
          val defaultConf: Map[String, Any] = Map("report.outputPath" -> reportPath) ++ DefaultConfigs.staticResourceConfig(appHome)
          configFile match {
            case Some(conf) => scenarios.+=(result.copy(className = csName, config = ConfigReader.fromYAML(conf) ++ defaultConf))
            case None => scenarios.+=(result.copy(className = csName, config = defaultConf))
          }

        }
      }
    })
    //Start executing the test
    runner.run(scenarios)

  }

}
