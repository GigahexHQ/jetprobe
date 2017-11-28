package com.jetprobe.mongo.action

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.jetprobe.core.action.{Action, ExecutableAction}
import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.mongo.sink.MongoSink

/**
  * @author Shad.
  */
class MongoDBActionBuilder(actionDef : MongoIOActionDef,sink : MongoSink) extends ActionBuilder{
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val actorProps = MongoIOActor.props(next,sink)
    val actor = ctx.system.actorOf(actorProps,"MongoDBAction-" + new UUIDGenerator().generateId(this).toString)
    new ExecutableAction(actionDef,actor)
  }

}
