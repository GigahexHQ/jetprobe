package com.jetprobe.core.common

/**
  * @author Shad.
  */
object DefaultConfigs {

  val cssPathAttr = "report.cssPaths"
  val jsPathAttr = "report.jsPaths"
  val htmlReportAttr = "report.outputPath"
  val defaultCssPath = "/static/css/materialize.min.css,/static/css/style.css"
  val defaultJsPaths = "/static/js/jquery.min.js," +
    "/static/js/materialize.min.js,/static/js/progressbar.min.js,/static/js/init.js"

  def staticResourceConfig(appRoot: String) = Map(cssPathAttr -> defaultCssPath.split(",").map(appRoot + _).mkString(","),
    jsPathAttr -> defaultJsPaths.split(",").map(appRoot + _).mkString(","))

}
