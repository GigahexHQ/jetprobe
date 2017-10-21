package com.jetprobe.core.action.builder

import com.jetprobe.core.action.{Action, FeedConsumer, Ingest}
import com.jetprobe.core.sink.DataSink
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
class IngestionBuilder(sink: DataSink) extends ActionBuilder {

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val actor = ctx.system.actorOf(FeedConsumer.props(sink))
    new Ingest(actor, next)
  }
}
