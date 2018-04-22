package com.jetprobe.core

import com.jetprobe.core.task.builder.{RunnableTaskBuilder, TaskBuilder}
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.http.{HttpDSL, HttpSupport}
import com.jetprobe.core.structure._

/**
  * @author Shad.
  */
trait CoreDsl
  extends HttpSupport
    with DataFeed[PipelineBuilder]
    with SecureShell[PipelineBuilder]
    with HttpDSL[PipelineBuilder]
    with Validations[PipelineBuilder]
    with Pauses[PipelineBuilder]
    with LocalCommands[PipelineBuilder] {

  var scn: PipelineBuilder = {
    val cls = getClass
    val annotation = cls.getAnnotation(classOf[PipelineMeta])
    annotation match {
      case ann if (ann != null) => PipelineBuilder(ann.name(), cls.getName)
      case _ => PipelineBuilder(cls.getName, cls.getName)
    }
  }

  override private[core] def taskBuilders = scn.taskBuilders

  override def chain(newTaskBuilders: Seq[TaskBuilder]): PipelineBuilder = {
    scn =
      scn.copy(taskBuilders = newTaskBuilders.toList ::: scn.taskBuilders)
    scn

  }

}

object Predef {

  type Session = com.jetprobe.core.session.Session

}
