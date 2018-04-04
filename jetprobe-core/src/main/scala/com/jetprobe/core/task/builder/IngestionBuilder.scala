package com.jetprobe.core.task.builder

import com.jetprobe.core.task._
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.session.Session
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, PipelineContext}

/**
  * @author Shad.
  */
class IngestionBuilder(datasetGen : DataGenerator, writer : Iterator[String] => Unit) extends TaskBuilder {

  val name = "IngestTask"

  def handleIngest(message : TaskMessage,session: Session) : Session = {
    val result = message match {
      case IngestTaskMessage(dg) => dg.generate(session)
    }

    result match {
      case Some(data) => writer.apply(data)
      case None => throw new Exception("Unable to generate data")
    }

    session
  }

  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: Task): Task = {

    val msg = IngestTaskMessage(datasetGen)
    new SelfExecutableTask(name,msg,next,ctx.system,ctx.controller) (handleIngest)
  }


}

class StorageIOBuilder[S <: Storage](datasetGen : DataGenerator,conf : Config[S], handler : (Iterator[String],S) => Unit) extends TaskBuilder with TaskMessage {

  def handleIngest(message: TaskMessage,session: Session) : Session = message match {
    case task : StorageIOBuilder[S] =>
      val data = datasetGen.generate(session)
      data.map { dataset =>

        val storage = conf.getStorage(session.attributes)
        handler.apply(dataset,storage)
      }
      session


  }

  override def build(ctx: PipelineContext, next: Task): Task = {

    new SelfExecutableTask(name,this,next,ctx.system,ctx.controller)(handleIngest)

  }

  override def name: String = s"Ingest-data-with-generator-${datasetGen.getClass.getSimpleName}-${conf.getClass.getSimpleName}"
}

case class IngestTaskMessage(dataGen : DataGenerator) extends TaskMessage {

  override def name: String = s"Ingest data with generator ${dataGen.getClass.getSimpleName}"
}