package com.jetprobe.core.task.builder

import com.jetprobe.core.task.{Task, TaskMessage, SelfExecutableTask}
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, PipelineContext}

/**
  * @author Shad.
  */
class RunnableTaskBuilder[T <: Storage](storageConf: Config[T], handler: T => Unit) extends TaskBuilder {


  val name: String = "RunnableTask"

  /**
    *
    * @param ctx the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: Task): Task = {

    val runnableMessage = RunnableTaskMessage(storageConf, handler)

    new SelfExecutableTask(name, runnableMessage, next, ctx.system, ctx.controller)({
      case (message, sess) => message match {
        case r: RunnableTaskMessage[T] =>
          val storage = r.storageConf.getStorage(sess.attributes)
          r.handler.apply(r.storageConf.getStorage(sess.attributes))
          storage.cleanup
          sess

      }
    }
    )

  }
}

case class RunnableTaskMessage[T <: Storage](storageConf: Config[T], handler: T => Unit) extends TaskMessage {

  override def name: String = s"Runnable Task on target type: ${storageConf.getClass.getSimpleName}"

}