package com.jetprobe.core.runner

import java.io.File
import java.net.{URL, URLClassLoader}

import scala.collection.JavaConverters._
import java.io.FileInputStream
import java.lang.reflect.Modifier
import java.util
import java.util.zip.ZipInputStream

import akka.actor.ActorSystem
import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
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


    appHome = System.getProperty("prog.home")

    //TODO Better approach to show the version
    if (args.length == 1 && (args(0) == "-v" || args(0) == "--version")) {
      println(getProgramVersion)
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

  def getProgramVersion: String = {
    val progName = System.getProperty("prog.name", "jetprobe")
    val version = System.getProperty("prog.version")
    val revision = System.getProperty("prog.revision")
    s"$progName $version \nbuild : $revision"
  }

  def extractAndRun(jarPath: String, configFile: Option[File], reportPath: String): Unit = {
    val classNames = new util.ArrayList[String]
    val zip = new ZipInputStream(new FileInputStream(jarPath))
    var entry = zip.getNextEntry
    while ( {
      entry != null
    }) {
      if (!entry.isDirectory && entry.getName.endsWith(".class") && !entry.getName.contains("HttpReqs")) {
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

    classNames.asScala.foreach(csName => {
      val testBuilder = classLoader.loadClass(csName)
      val isAbstract = Modifier.isAbstract(testBuilder.getModifiers)
      if (testBuilder.isAnnotationPresent(classOf[TestSuite]) && !isAbstract) {
        val t = testBuilder.newInstance()

        if (t.isInstanceOf[TestScenario]) {
          val m = testBuilder.getDeclaredMethod("buildScenario")
          m.setAccessible(true)
          val result = m.invoke(t).asInstanceOf[ExecutableScenario]
          val defaultConf: Map[String, Any] = Map(DefaultConfigs.htmlReportAttr -> reportPath) ++ DefaultConfigs.staticResourceConfig(appHome)
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
