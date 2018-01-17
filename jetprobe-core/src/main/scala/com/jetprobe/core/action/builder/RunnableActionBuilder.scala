package com.jetprobe.core.action.builder

import com.jetprobe.core.action.{Action, ActionMessage, SelfExecutableAction}
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, ScenarioContext}

/**
  * @author Shad.
  */
class RunnableActionBuilder[T <: Storage](storageConf: Config[T], handler: T => Unit) extends ActionBuilder {


  val name: String = "RunnableAction"

  /**
    *
    * @param ctx the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {

    val runnableMessage = RunnableActionMessage(storageConf, handler)

    new SelfExecutableAction(name, runnableMessage, next, ctx.system, ctx.controller)({
      case (message, sess) => message match {
        case r: RunnableActionMessage[T] =>
          val storage = r.storageConf.getStorage(sess.attributes)
          r.handler.apply(r.storageConf.getStorage(sess.attributes))
          storage.cleanup
          sess

      }
    }
    )

  }
}

case class RunnableActionMessage[T <: Storage](storageConf: Config[T], handler: T => Unit) extends ActionMessage {

  override def name: String = s"Runnable Action on target type: ${storageConf.getClass.getSimpleName}"

}