package com.jetprobe.core.action.builder

import com.jetprobe.core.action._
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.session.Session
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, PipelineContext}

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
  override def build(ctx: PipelineContext, next: Action): Action = {

    val msg = IngestActionMessage(datasetGen)
    new SelfExecutableAction(name,msg,next,ctx.system,ctx.controller) (handleIngest)
  }


}

class StorageIOBuilder[S <: Storage](datasetGen : DataGenerator,conf : Config[S], handler : (Iterator[String],S) => Unit) extends ActionBuilder with ActionMessage {

  def handleIngest(message: ActionMessage,session: Session) : Session = message match {
    case action : StorageIOBuilder[S] =>
      val data = datasetGen.generate(session)
      data.map { dataset =>

        val storage = conf.getStorage(session.attributes)
        handler.apply(dataset,storage)
      }
      session


  }

  override def build(ctx: PipelineContext, next: Action): Action = {

    new SelfExecutableAction(name,this,next,ctx.system,ctx.controller)(handleIngest)

  }

  override def name: String = s"Ingest-data-with-generator-${datasetGen.getClass.getSimpleName}-${conf.getClass.getSimpleName}"
}

case class IngestActionMessage(dataGen : DataGenerator) extends ActionMessage {

  override def name: String = s"Ingest data with generator ${dataGen.getClass.getSimpleName}"
}