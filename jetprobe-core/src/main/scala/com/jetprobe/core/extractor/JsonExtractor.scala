package com.jetprobe.core.extractor

import java.util.{LinkedHashMap => jl}

import com.jayway.jsonpath.{Configuration, JsonPath}
import com.typesafe.scalalogging.LazyLogging
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
object JsonPathExtractor extends DataExtractor[String,Map[String,Any]]{

  def jsonPath(path : String, saveAs : String) : JsonPathBuilder = new JsonPathBuilder(path,saveAs)

  def extractJsonVal(path : String, fromDoc : jl[String,Any]) : Any = {

    if(fromDoc.get(path) == null){
      throw new Exception(s"${path} not found")
    } else
      fromDoc.get(path)
  }

}

class JsonPathBuilder(val path : String, saveAs : String) extends LazyLogging{

  def extractFrom[T](json : String) : Map[String,T] = {
    val document = Configuration.defaultConfiguration.jsonProvider.parse(json)
    val result = JsonPath.using(Configuration.defaultConfiguration).parse(json).read[T](path)
    val extractedVal = Try {result}
    extractedVal match {
      case Success(value) =>
        logger.info(s"Extracted value for path ${path} = $value")
        Map(saveAs -> value)
      case Failure(ex) =>
        logger.error(s"Exception occurred while extracting path = ${path}. Message : ${ex.getMessage}")
        logger.error(s"Unable to parse string : ${json}")
        Map.empty
    }

  }



}