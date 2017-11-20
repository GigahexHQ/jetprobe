package com.jetprobe.core.extractor

import com.jayway.jsonpath.{Configuration, JsonPath}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
object JsonPathExtractor extends DataExtractor[String,Map[String,Any]]{

  def jsonPath(path : String, saveAs : String) : JsonPathBuilder = new JsonPathBuilder(path,saveAs)

}

class JsonPathBuilder(val path : String, saveAs : String) extends LazyLogging{

  def extractFrom(json : String) : Map[String,Any] = {
    val document = Configuration.defaultConfiguration.jsonProvider.parse(json)
    val extractedVal = Try {JsonPath.read[Any](document,path)}
    extractedVal match {
      case Success(value) =>
        logger.info(s"Extracted value for path ${path} = $value")
        Map(saveAs -> value)
      case Failure(ex) =>
        logger.error(s"Exception occurred while extracting path = ${path}. Message : ${ex.getMessage}")
        logger.error(s"Unable to parse string : ${json}")
        Map.empty[String,Any]
    }

  }



}