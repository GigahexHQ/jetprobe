package com.jetprobe.core.structure

import com.jetprobe.core.task.ExecutableTask
import com.jetprobe.core.http.HttpDSL

/**
  * @author Shad.
  */
trait StructureBuilder[B <: StructureBuilder[B]]
  extends Execs[B]
    with DataFeed[B]
    with HttpDSL[B]
    with Pauses[B]
    with SecureShell[B]
    with Validations[B]
    with LocalCommands[B] {

  private[jetprobe] def build(ctx: PipelineContext,
                              chainNext: ExecutableTask): ExecutableTask = {
    taskBuilders.foldLeft(chainNext) { (next, taskBuilder) =>
      taskBuilder.build(ctx, next)
    }
  }

}
