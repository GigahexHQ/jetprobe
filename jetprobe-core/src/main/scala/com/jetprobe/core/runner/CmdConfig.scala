package com.jetprobe.core.runner

import java.io.File

/**
  * @author Shad.
  */
case class CmdConfig(testableJar : File = new File("."), configFile : Option[File] = None, reportPath : String = "./report.html")

object CmdConfig {

  val parser = new scopt.OptionParser[CmdConfig]("jetprobe") {
    head("jetprobe", "0.1-SNAPSHOT")

    opt[File]('t', "testjar").required().valueName("<file>").
      action( (x, c) => c.copy(testableJar = x) ).
      text("Testable jar is required")

    opt[File]('c', "config").valueName("<config>").
      action( (x, c) => c.copy(configFile = Some(x)) ).
      text("Test config file path")

    opt[String]('r', "reportPath").valueName("<path>").
      action( (x, c) => c.copy(reportPath = x) ).
      text("Report output file path")

    help("help").text("prints this usage text")

  }

}
