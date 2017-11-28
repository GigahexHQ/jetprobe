package com.jetprobe.core.action.builder

import com.jetprobe.core.action.HttpRequestAction.HttpRequestMessage
import com.jetprobe.core.action.{Action, ExecutableAction, HttpExecutor, HttpRequestAction}
import com.jetprobe.core.http.HttpRequestBuilder
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
class HttpRequestActionBuilder(requestBuilder : HttpRequestBuilder) extends ActionBuilder{
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val actorRef = ctx.system.actorOf(HttpExecutor.props(next))
    val httpMessage = HttpRequestMessage(requestBuilder)
    new ExecutableAction(httpMessage,actorRef)
  }
}
