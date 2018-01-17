package com.jetprobe.core.action.builder

import com.jetprobe.core.action.Action
import com.jetprobe.core.structure.ScenarioContext
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
trait ActionBuilder extends LazyLogging{

  /**
    * @param ctx the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  def build(ctx: ScenarioContext, next: Action): Action
}
