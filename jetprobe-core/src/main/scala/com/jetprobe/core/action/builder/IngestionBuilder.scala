package com.jetprobe.core.action.builder

import com.jetprobe.core.action._
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
class IngestionBuilder(datasetGen : DataGenerator, writer : Iterator[String] => Unit) extends ActionBuilder {

  val name = "IngestAction"

  def handleIngest(message : ActionMessage,session: Session) : Session = {
    val result = message match {
      case IngestActionMessage(dg) => dg.generate(session)
    }

    result match {
      case Some(data) => writer.apply(data)
      case None => throw new Exception("Unable to generate data")
    }

    session
  }

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {

    val msg = IngestActionMessage(datasetGen)
    new SelfExecutableAction(name,msg,next,ctx.system,ctx.controller) (handleIngest)
  }


}

case class IngestActionMessage(dataGen : DataGenerator) extends ActionMessage {

  override def name: String = s"Ingest data with generator ${dataGen.getClass.getSimpleName}"
}