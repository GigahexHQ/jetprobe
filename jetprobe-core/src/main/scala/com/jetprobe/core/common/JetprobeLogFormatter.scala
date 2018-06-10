package com.jetprobe.core.common

import java.io.{PrintWriter, StringWriter}
import java.util.regex.Pattern

import wvlet.log.LogLevel.{DEBUG, ERROR, INFO, TRACE, WARN}
import wvlet.log.{LogFormatter, LogLevel, LogRecord}

/**
  * @author Shad.
  */
object JetprobeLogFormatter extends LogFormatter{

  private val testFrameworkFilter = Pattern.compile("""\s+at (sbt\.|org\.scalatest\.).*""")
  val DEFAULT_STACKTRACE_FILTER: String => Boolean = { line: String =>
    !testFrameworkFilter.matcher(line).matches()
  }
  private var stackTraceFilter: String => Boolean = DEFAULT_STACKTRACE_FILTER


  override def formatLog(r: LogRecord): String = {
    val logTag = highlightLog(r.level, r.level.name.toUpperCase)
    val log    = f"${withColor(Console.BLUE, formatTimestamp(r.getMillis))} ${logTag}%14s [${withColor(Console.WHITE, r.leafLoggerName)}] ${highlightLog(r.level, r.getMessage)}"
    appendStackTrace(log, r)
  }

  def withColor(prefix: String, s: String) = {
    s"${prefix}${s}${Console.RESET}"

  }

  def setStackTraceFilter(filter: String => Boolean) {
    stackTraceFilter = filter
  }

  def appendStackTrace(m: String, r: LogRecord): String = {
    r.cause match {
      case Some(ex) =>
        s"${m}\n${highlightLog(r.level, formatStacktrace(ex))}"
      case None =>
        m
    }
  }

  def formatStacktrace(e: Throwable): String = {
    val trace = new StringWriter()
    e.printStackTrace(new PrintWriter(trace))
    val stackTrace = trace.toString
    val filtered =
      stackTrace
        .split("\n") // Array
        .filter(stackTraceFilter)
        .sliding(2)
        .collect { case Array(a, b) if a != b => a }

    filtered.mkString("\n")
  }

  def formatTimestamp(l: Long) : String = {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    format.format(l)
  }

  def highlightLog(level: LogLevel, message: String): String = {
    val color = level match {
      case ERROR => Console.RED
      case WARN  => Console.YELLOW
      case INFO  => Console.CYAN
      case DEBUG => Console.GREEN
      case TRACE => Console.MAGENTA
      case _     => Console.RESET
    }
    withColor(color, message)
  }
}
