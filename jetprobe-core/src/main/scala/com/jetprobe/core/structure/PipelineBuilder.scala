package com.jetprobe.core.structure

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.action.Action
import com.jetprobe.core.action.builder.ActionBuilder

/**
  * @author Shad.
  */
case class PipelineBuilder(name: String,
                           cls: String,
                           actionBuilders: List[ActionBuilder] = List.empty) {

  private[core] def newInstance(actionBuilders: List[ActionBuilder]) =
    copy(actionBuilders = actionBuilders)


  def build(): ExecutablePipeline = ExecutablePipeline(this, this.cls)

  private[jetprobe] def build(ctx: PipelineContext,
                              chainNext: Action): Action = {
    actionBuilders.foldLeft(chainNext) { (next, actionBuilder) =>
      actionBuilder.build(ctx, next)
    }
  }

}

case class ExecutablePipeline(PipelineBuilder: PipelineBuilder, className: String, config: Map[String, Any] = Map.empty) {

  def build(system: ActorSystem,
            onExit: Action,
            controller: ActorRef): Scenario = {
    val ctx = PipelineContext(system, controller)
    val entry = PipelineBuilder.build(ctx, onExit)
    Scenario(PipelineBuilder.name, entry, ctx, className, config)

  }
}

case class PipelineContext(system: ActorSystem, controller: ActorRef)


trait Sample[T] {

  def getName(s: String): T = ???
}