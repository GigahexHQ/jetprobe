package com.jetprobe.core

import com.jetprobe.core.action.builder.{ActionBuilder, RunnableActionBuilder}
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.http.{HttpDSL, HttpSupport}
import com.jetprobe.core.structure._


/**
  * @author Shad.
  */
trait CoreDsl extends HttpSupport
  with DataFeed[ScenarioBuilder]
  with SecureShell[ScenarioBuilder]
  with HttpDSL[ScenarioBuilder]
  with Validations[ScenarioBuilder]
  with Pauses[ScenarioBuilder] {

  var scn: ScenarioBuilder = {
    val cls = getClass
    val annotation = cls.getAnnotation(classOf[TestSuite])
    annotation match {
      case ann if(ann != null) => ScenarioBuilder(ann.name(),cls.getName)
      case _ => ScenarioBuilder(cls.getName,cls.getName)
    }
  }

  override private[core] def actionBuilders = scn.actionBuilders

  override def chain(newActionBuilders: Seq[ActionBuilder]): ScenarioBuilder = {
    scn = scn.copy(actionBuilders = newActionBuilders.toList ::: scn.actionBuilders)
    scn

  }


}

object Predef {

  type Session = com.jetprobe.core.session.Session


}
