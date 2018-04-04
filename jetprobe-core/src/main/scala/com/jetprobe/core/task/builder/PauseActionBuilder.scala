package com.jetprobe.core.task.builder

import com.jetprobe.core.task.{Task, Pause}
import com.jetprobe.core.structure.PipelineContext

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * @author Shad.
  */
class PauseTaskBuilder(duration : FiniteDuration) extends TaskBuilder{
  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: Task): Task = new Pause(duration,next,ctx.system, ctx.controller)
}
