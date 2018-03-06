package com.jetprobe.core

import com.jetprobe.core.action.builder.{ActionBuilder, RunnableActionBuilder}
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.http.{HttpDSL, HttpSupport}
import com.jetprobe.core.structure._


/**
  * @author Shad.
  */
trait CoreDsl extends HttpSupport
  with DataFeed[PipelineBuilder]
  with SecureShell[PipelineBuilder]
  with HttpDSL[PipelineBuilder]
  with Validations[PipelineBuilder]
  with Pauses[PipelineBuilder] {

  var scn: PipelineBuilder = {
    val cls = getClass
    val annotation = cls.getAnnotation(classOf[TestSuite])
    annotation match {
      case ann if(ann != null) => PipelineBuilder(ann.name(),cls.getName)
      case _ => PipelineBuilder(cls.getName,cls.getName)
    }
  }

  override private[core] def actionBuilders = scn.actionBuilders

  override def chain(newActionBuilders: Seq[ActionBuilder]): PipelineBuilder = {
    scn = scn.copy(actionBuilders = newActionBuilders.toList ::: scn.actionBuilders)
    scn

  }


}

object Predef {

  type Session = com.jetprobe.core.session.Session


}
