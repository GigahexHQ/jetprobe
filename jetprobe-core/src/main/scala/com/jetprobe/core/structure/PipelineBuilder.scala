package com.jetprobe.core.structure

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.task.Task
import com.jetprobe.core.task.builder.TaskBuilder

/**
  * @author Shad.
  */
case class PipelineBuilder(name: String,
                           cls: String,
                           taskBuilders: List[TaskBuilder] = List.empty) {

  private[core] def newInstance(taskBuilders: List[TaskBuilder]) =
    copy(taskBuilders = taskBuilders)


  def build(): ExecutablePipeline = ExecutablePipeline(this, this.cls)

  private[jetprobe] def build(ctx: PipelineContext,
                              chainNext: Task): Task = {
    taskBuilders.foldLeft(chainNext) { (next, taskBuilder) =>
      taskBuilder.build(ctx, next)
    }
  }

}

case class ExecutablePipeline(PipelineBuilder: PipelineBuilder, className: String, config: Map[String, Any] = Map.empty) {

  def build(system: ActorSystem,
            onExit: Task,
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