package com.jetprobe.core.action.builder

import akka.actor.ActorRef
import com.jetprobe.core.action.{Action, Feed, FeedReceiver}
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.core.util.GetName

/**
  * @author Shad.
  */
class FeedBuilder(dataGenerator: DataGenerator)
    extends ActionBuilder
    with GetName {

  private[this] def newFeedReceiver(ctx: ScenarioContext): ActorRef = {
    val props = FeedReceiver.props(dataGenerator.build(ctx))
    ctx.system.actorOf(props)
  }
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val feedReceiver = newFeedReceiver(ctx)
    new Feed(feedReceiver, next)
  }

}
