package com.jetprobe.core.common

import java.io.{File, FileInputStream}
import java.util
import java.util.Properties

import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._
import scala.collection.mutable

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

trait ConfigReader {

  def fromFile(path : String) : Map[String,Any]

}

object YamlConfigReader extends ConfigReader {

  override def fromFile(path: String): Map[String, Any] = {
    val yaml = new Yaml()
    val inputStream = new FileInputStream(new File(path))
    val list = yaml.load[util.LinkedHashMap[String,Any]](inputStream).asScala
    inputStream.close()
    list.toMap[String,Any]
  }

}

object PropertiesConfigReader extends ConfigReader {

  override def fromFile(path: String): Map[String, Any] = {
    val config = mutable.Map.empty[String,Any]
    val fis = new FileInputStream(new File(path))
    val props = new Properties()
    props.load(fis)
    val enuKeys = props.keys()
    while ( {
      enuKeys.hasMoreElements
    }) {
      val key = enuKeys.nextElement.asInstanceOf[String]
      val value = props.getProperty(key)
      config +(key -> value)
    }

    config.toMap
  }
}
