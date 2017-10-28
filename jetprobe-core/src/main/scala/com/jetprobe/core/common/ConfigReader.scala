package com.jetprobe.core.common

import java.io.{File, FileInputStream}
import java.util

import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._

/**
  * @author Shad.
  */
object ConfigReader {

  def fromYAML(filePath : String) : Map[String,Any] = {
    val yaml = new Yaml()
    val inputStream = new FileInputStream(new File(filePath))
    val list = yaml.load[util.LinkedHashMap[String,Any]](inputStream).asScala
    list.toMap[String,Any]
  }

  def fromYAML(conf : File) : Map[String,Any] = {
    val yaml = new Yaml()
    val inputStream = new FileInputStream(conf)
    val list = yaml.load[util.LinkedHashMap[String,Any]](inputStream).asScala
    list.toMap[String,Any]
  }


}
