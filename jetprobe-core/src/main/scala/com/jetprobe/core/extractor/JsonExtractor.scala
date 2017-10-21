package com.jetprobe.core.extractor

import com.jayway.jsonpath.{Configuration, JsonPath}

/**
  * @author Shad.
  */
object JsonPathExtractor extends DataExtractor[String,Map[String,Any]]{

  def jsonPath(path : String, saveAs : String) : JsonPathBuilder = new JsonPathBuilder(path,saveAs)

}

class JsonPathBuilder(val path : String, saveAs : String) {

  def extractFrom(json : String) : Map[String,Any] = {
    val document = Configuration.defaultConfiguration.jsonProvider.parse(json)
    Map(saveAs -> JsonPath.read[Any](document,path))
  }



}