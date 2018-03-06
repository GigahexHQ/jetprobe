package com.jetprobe.core.action.builder

import com.jetprobe.core.action.{Action, Pause}
import com.jetprobe.core.structure.PipelineContext

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * @author Shad.
  */
class PauseActionBuilder(duration : FiniteDuration) extends ActionBuilder{
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: PipelineContext, next: Action): Action = new Pause(duration,next,ctx.system, ctx.controller)
}
