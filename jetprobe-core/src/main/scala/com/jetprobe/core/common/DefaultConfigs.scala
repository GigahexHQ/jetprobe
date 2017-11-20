package com.jetprobe.core.common

/**
  * @author Shad.
  */
object DefaultConfigs {

  val cssPathAttr = "report.cssPaths"
  val jsPathAttr = "report.jsPaths"
  val htmlReportAttr = "report.outputPath"
  val defaultCssPath = "/static/css/materialize.min.css,/static/css/style.css"
  val defaultJsPaths = "/static/js/progressbar.min.js,/static/js/init.js"
  val jqPath = "https://code.jquery.com/jquery-2.2.4.min.js"
  val materializeJs = "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/js/materialize.min.js"

  def staticResourceConfig(appRoot: String) = Map(cssPathAttr -> defaultCssPath.split(",").map(appRoot + _).mkString(","),
    jsPathAttr -> (jqPath + "," + materializeJs +","+ (defaultJsPaths.split(",").map(appRoot + _).mkString(","))))

}
