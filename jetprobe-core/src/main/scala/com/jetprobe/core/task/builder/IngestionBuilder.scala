package com.jetprobe.core.task.builder

import com.jetprobe.core.task._
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.session.Session
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, PipelineContext}

/**
  * @author Shad.
  */
case object IngestionTask extends TaskType

class IngestionBuilder(val description : String, datasetGen : DataGenerator, writer : Iterator[String] => Unit) extends TaskBuilder {

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
  override def build(ctx: PipelineContext, next: ExecutableTask): ExecutableTask = {

    val msg = IngestTaskMessage(datasetGen)
    val taskMeta = TaskMeta(description,IngestionTask)
    new SelfExecutableTask(taskMeta,msg,next,ctx.system,ctx.controller) (handleIngest)
  }


}

case object StorageIOTask extends TaskType

class StorageIOBuilder[S <: Storage](val description : String,datasetGen : DataGenerator,conf : Config[S], handler : (Iterator[String],S) => Unit)
  extends TaskBuilder with TaskMessage {

  def handleIngest(message: TaskMessage,session: Session) : Session = message match {
    case task : StorageIOBuilder[S] =>
      val data = datasetGen.generate(session)
      data.map { dataset =>

        val storage = conf.getStorage(session.attributes)
        handler.apply(dataset,storage)
      }
      session

  }

  override def build(ctx: PipelineContext, next: ExecutableTask): ExecutableTask = {

    val taskMeta = TaskMeta(name,StorageIOTask)
    new SelfExecutableTask(taskMeta,this,next,ctx.system,ctx.controller)(handleIngest)

  }

  override def name: String = s"Data Ingestion with ${datasetGen.getClass.getSimpleName}"
}

case class IngestTaskMessage(dataGen : DataGenerator) extends TaskMessage {

  override def name: String = s"Ingest data with generator ${dataGen.getClass.getSimpleName}"
}