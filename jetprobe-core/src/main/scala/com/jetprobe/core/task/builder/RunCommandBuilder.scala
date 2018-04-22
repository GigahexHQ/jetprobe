package com.jetprobe.core.task.builder

import java.io.File

import com.jetprobe.core.Predef.Session
import com.jetprobe.core.task._
import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.structure.PipelineContext

/**
  * @author Shad.
  */
case object LocalCommandTask extends TaskType

class RunCommandBuilder(val description : String,command: String, at: String) extends TaskBuilder {
  private val name = "Local Command Task"
  private val windowsCmd = "cmd /C "

  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: Task): Task = {
    val message = RunCommandMessage(command, at)
    val taskMeta = TaskMeta(description,LocalCommandTask)
    new SelfExecutableTask(taskMeta, message, next, ctx.system, ctx.controller)(handleMessage)
  }

  import sys.process._

  private def handleMessage(message: TaskMessage, session: Session): Session = message match {
    case RunCommandMessage(cmd: String, at: String) => ExpressionParser.parseAll(Seq(Expr(cmd), Expr(at)), session.attributes) match {
      case Left(ex) => throw ex
      case Right(parsedMap) =>
        val cd = parsedMap(at).trim
        //Execute the cd
        val isWindows = System.getProperty("os.name").startsWith("Window")
        val cdCmd = s"cd ${cd} "
        val output = if (isWindows)
          Process("cmd /C cd " + cd).!
        else Process(s"cd ${cd}")

        val exec = if (isWindows) windowsCmd + cmd else cmd

        Process(exec,new File(cd)).!(ProcessLogger(println(_)))

        session
    }
    case _ => throw new UnsupportedOperationException("Command must be provided.")
  }
}

case class RunCommandMessage(cmd: String, at: String) extends TaskMessage {
  override def name: String = s"Execute ${cmd}"


}