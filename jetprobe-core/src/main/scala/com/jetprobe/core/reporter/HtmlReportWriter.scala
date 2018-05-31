package com.jetprobe.core.reporter

import java.awt.Desktop
import java.io.{File, PrintWriter}

import com.jetprobe.core.common.DefaultConfigs
import com.jetprobe.core.validations._

import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.Text.tags2.nav
import DefaultConfigs._

/**
  * @author Shad.
  */
class HtmlReportWriter(config: Map[String, Any], jpInstallPath : String,
                       outputPath : String,
                       project : String) extends ResultReporter[File] {

  import HtmlReportWriter._
  val reportFile = project.replaceAll(" ","-")


  private val materialCss = "https://cdnjs.cloudflare.com/ajax/libs/materialize/0.100.2/css/materialize.css"
  private val defaultCssPaths = Seq("/static/css/style.css")

  override def write(reports: Seq[ValidationReport]): File = {

    val cssPaths = Seq(materialCss) ++ defaultCssPaths.map{ path =>
      val modPath = if(Desktop.isDesktopSupported) path.replace("/","\\") else path
      jpInstallPath + modPath
    }
    val htmlConf = staticResourceConfig(jpInstallPath)
    val jsPaths = htmlConf.get(jsPathAttr) match {
      case Some(path) => path.toString.split(",").toSeq
      case None => defaultJsPaths.split(",").toSeq
    }

    val statsBar = validationStats(reports)
    val resultPanel = for (report <- reports) yield validationResultList(report.suite, report.detailReport)
    val htmlContent = "<!DOCTYPE html>" + html(lang := "en")(
      docHead("Jet Probe | Test Suites Report", cssPaths, jsPaths),
      body(cls := "dashboard")(
        //TODO take this property from some config
        navigation("Jet Probe", "https://jetprobe.com"),
        mainBody(docSideBar(reports,project), statsBar, resultPanel),
        for (js <- jsPaths) yield script(src := js)
      )

    )

    //val outputPath = config(HtmlReportWriter.htmlReportPath).toString

    val dir = new File(outputPath)
    dir.mkdir()
    val reportFileInstance = new File(outputPath + "/" + reportFile + ".html")
    val pw = new PrintWriter(reportFileInstance)
    pw.write(htmlContent)
    pw.close
    reportFileInstance

  }

}

object HtmlReportWriter {

  val htmlReportPath = "reporter.html.outputPath"
  var project : String = _

  def docHead(title: String, cssPaths: Seq[String], jsPaths: Seq[String]): TypedTag[String] = {
    head(
      meta(httpEquiv := "Content-type", content := "text/html;charset=UTF-8"),
      meta(name := "viewport", content := "width=device-width, initial-scale=1, maximum-scale=1.0"),
      meta(name := "title", content := title),
      meta(name := "description", content := "Jetprobe Result report"),
      link(href := "https://fonts.googleapis.com/icon?family=Material+Icons", rel := "stylesheet"),
      for (cssPath <- cssPaths) yield link(href := cssPath, rel := "stylesheet")


    )

  }

  private[reporter] def mainBody(sideBar: TypedTag[String],
                                 statsBars: TypedTag[String],
                                 resultPanels: Seq[TypedTag[String]]): TypedTag[String] = {
    div(cls := "section no-pad-bot", id := "index-banner")(
      div(cls := "container main-container", style := "min-height:80vh;")(
        div(cls := "row")(
          sideBar,
          div(cls := "col s8 main-section-wrapper")(
            statsBars,
            for (resultPanel <- resultPanels) yield resultPanel
          )
        )
      )
    )
  }


  private[reporter] def docSideBar(suites: Seq[ValidationReport],project : String): TypedTag[String] = {
    def testSuites(status: String, name: String, active : Boolean): TypedTag[String] = {
      val className = if(active) "active" else ""
      li(data.suite := s"${getShortenedName(name)}", cls := className)(
        div(cls := s"$status ring"),
        span(name)
      )
    }
    val activeSuite = testSuites(suites.head.finalSatus.toString.toLowerCase,suites.head.suite,true)
    val allSuites = {
      if(suites.size > 1)
        Seq(activeSuite) ++ suites.drop(1).map(rep => testSuites(rep.finalSatus.toString.toLowerCase,rep.suite,false))
      else
      Seq(activeSuite)
    }

    div(cls := "card col s3", style := "margin-right:20px;")(
      div(cls := "card-content")(
        span(cls := "card-title")(s"${project} scenario"),
        div(cls := "nav-options")(
          ul(
            for (suiteReport <- allSuites) yield suiteReport
          )
        )
      )
    )
  }

  private[reporter] def getShortenedName(name: String): String = name.toLowerCase.split(" ").mkString("-")

  /**
    * Creates the top navigation
    *
    * @param brand
    * @param brandURI
    * @return
    */
  private[reporter] def navigation(brand: String, brandURI: String): TypedTag[String] = {
    nav(cls := "white", role := "navigation")(
      div(cls := "nav-wrapper container")(
        a(id := "logo-container", href := s"$brandURI", cls := "brand-logo black-text")(s"$brand"),
        ul(cls := "right hide-on-med-and-down")(
          li(
            a(cls := "grey-text text-darken-2", href := "https://jetprobe.com/docs/introduction/")("Docs")
          ),
          li(
            a(cls := "grey-text text-darken-2", href := "https://github.com/jetprobe/jetprobe")("Github")
          )
        ),
        ul(id := "nav-mobile", cls := "side-nav")(
          li(cls := "grey-text text-darken-4", style := "text-align: center;padding-top:20px; font-size:18px;")("Jet Probe"),
          li(
            a(href := "https://jetprobe.com/docs/introduction/")("Docs")
          ),
          li(
            a(href := "https://github.com/jetprobe/jetprobe")("Github")
          )
        ),
        a(href := "#", data.activates := "nav-mobile", cls := "button-collapse")(
          i(cls := "material-icons black-text")("menu")
        )
      )
    )

  }

  /**
    * Function to build the stats banner based on the status
    *
    * @param report Validation Report of all the test suites
    * @return
    */
  private[reporter] def statsSection(report: ValidationReport): Seq[TypedTag[String]] = {

    val shortenedName = getShortenedName(report.suite)
    val totalTests = report.detailReport.size.toFloat
    val passedFrac = "%.2f".format(report.successCount / totalTests)
    val failedFrac = "%.2f".format(report.failedCount / totalTests)
    val skippedFrac = "%.2f".format(report.skippedCount / totalTests)

    def singleStat(testSuite: String, status: String, fraction: String, count: Int): TypedTag[String] = {
      div(cls := "col s4 center-align hidden")(
        div(cls := "stats-container", data.status := status, data.fraction := fraction,
          attr("data-suite-parent") := shortenedName,
          attr("data-stat") := s"${count}")
      )
    }

    val stats = Seq(
      (shortenedName, "passed", passedFrac, report.successCount),
      (shortenedName, "failed", failedFrac, report.failedCount),
      (shortenedName, "skipped", skippedFrac, report.skippedCount)
    )

    stats.map(st => singleStat(st._1, st._2, st._3, st._4))

  }

  private[reporter] def validationStats(reports: Seq[ValidationReport]): TypedTag[String] = {
    val statsList = reports.flatMap(rep => statsSection(rep))
    div(cls := "row")(
      div(cls := "statusbar-top col s12 card", style := "height:140px;")(
        for (stat <- statsList) yield stat
      )
    )

  }

  private[reporter] def validationResultList(reportName: String, results: Seq[ValidationResult]): TypedTag[String] = {
    val groupedResult = results.groupBy(_.testName)
    val shortenedName = getShortenedName(reportName)


    def convertToSeq(convertible: Iterable[TypedTag[String]]): Seq[TypedTag[String]] = convertible.toSeq

    def groupValidationResults(testName: String, vResults: Seq[ValidationResult]): TypedTag[String] = {
      vResults match {
        case vrs if vrs.count(_.status == Passed) == vrs.size =>
          li(attr("data-suite-panel") := shortenedName)(
            div(cls := "collapsible-header")(
              i(cls := "material-icons passed")("check_circle"),
              s"${vrs.count(_.status == Passed)} Validations for ${testName}"),
            //details of the test
            div(cls := "collapsible-body")(
              p("All tests passed")
            )

          )

        case vrs if vrs.count(_.status == Skipped) > 0 =>
          li(attr("data-suite-panel") := shortenedName)(
            div(cls := "collapsible-header")(
              i(cls := "material-icons skipped")("info"),
              s"${vrs.count(_.status == Skipped)} Validations for ${testName}"),
            div(cls := "collapsible-body")(
              for (skippedTest <- vrs.filter(_.status == Skipped)) yield {
                p(s"${skippedTest.message}")
              }
            )
          )


        case vrs if vrs.count(_.status == Failed) > 0 =>
          li(attr("data-suite-panel") := shortenedName)(
            div(cls := "collapsible-header")(
              i(cls := "material-icons failed")("cancel"),
              s"${vrs.count(_.status == Failed)} Validations for ${testName}"),
            div(cls := "collapsible-body")(
              for (failedTest <- vrs.filter(_.status == Failed)) yield {
                p(s"${failedTest.message}")
              }
            )
          )

      }
    }

    div(cls := "row hidden result-panel", attr("data-results-panel") := getShortenedName(reportName))(
      ul(cls := "collapsible", attr("data-collapsible") := "accordion")(
        convertToSeq(for (res <- groupedResult) yield (groupValidationResults(res._1, res._2)))
      )
    )
  }


}
