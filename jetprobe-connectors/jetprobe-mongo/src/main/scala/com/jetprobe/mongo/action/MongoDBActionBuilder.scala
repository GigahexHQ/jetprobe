package com.jetprobe.mongo.action

import com.jetprobe.core.action.Action
import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
class MongoDBActionBuilder extends ActionBuilder{
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = ???

}
