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
import com.jetprobe.core.runner.TestRunner.executeTests
import com.jetprobe.core.structure.ExecutableScenario
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
object TestRunner extends LazyLogging {

  def main(args: Array[String]): Unit = {

    val parsedConfig = CmdConfig.parser.parse(args, CmdConfig())
    parsedConfig match {
      case Some(conf) => extractAndRun(conf.testableJar.getAbsolutePath)
      case None =>
        logger.info("Unable to parse the arguments")
        System.exit(1)
    }

    val jarPath = "C:\\Users\\samez\\Documents\\projects\\jetprobe\\jetprobe-sample\\target\\scala-2.12\\jetprobe-sample_2.12-1.0.jar"
  }


  def extractAndRun(jarPath: String): Unit = {
    val classNames = new util.ArrayList[String]
    val zip = new ZipInputStream(new FileInputStream(jarPath))
    var entry = zip.getNextEntry
    while ( {
      entry != null
    }) {
      if (!entry.isDirectory && entry.getName.endsWith(".class")) { // This ZipEntry represents a class. Now, what class does it represent?
        val className = entry.getName.replace('/', '.') // including ".class"
        classNames.add(className.substring(0, className.length - ".class".length))
      }

      entry = zip.getNextEntry
    }

    executeTests(jarPath, classNames)

  }

  def executeTests(jarPath: String, classNames: util.ArrayList[String]): Unit = {
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
          scenarios.+=(result.copy(className = csName))
        }
      }
    })
    //Start executing the test
    runner.run(scenarios)

  }

}
