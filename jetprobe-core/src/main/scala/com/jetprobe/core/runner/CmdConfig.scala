package com.jetprobe.core.runner

import java.io.File

/**
  * @author Shad.
  */
case class CmdConfig(testableJar : File = new File("."), configFile : File = new File("."))

object CmdConfig {

  val parser = new scopt.OptionParser[CmdConfig]("jetprobe") {
    head("jetprobe", "0.1")

    opt[File]('t', "testjar").required().valueName("<file>").
      action( (x, c) => c.copy(testableJar = x) ).
      text("test jar is a required file property")

    opt[File]('c', "config").valueName("<config-file>").
      action( (x, c) => c.copy(configFile = x) ).
      text("Test config file path")
  }

}
