package com.jetprobe.core.task.builder

import com.jetprobe.core.task.Task
import com.jetprobe.core.structure.PipelineContext
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
trait TaskBuilder extends LazyLogging {

  val description : String

  /**
    * @param ctx the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  def build(ctx: PipelineContext, next: Task): Task
}
