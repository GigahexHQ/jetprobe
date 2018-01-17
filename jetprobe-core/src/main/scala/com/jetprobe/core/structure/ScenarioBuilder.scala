package com.jetprobe.core.structure

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.action.Action
import com.jetprobe.core.action.builder.ActionBuilder

/**
  * @author Shad.
  */
case class ScenarioBuilder(name: String,
                           cls: String,
                           actionBuilders: List[ActionBuilder] = List.empty) {

  private[core] def newInstance(actionBuilders: List[ActionBuilder]) =
    copy(actionBuilders = actionBuilders)


  def build(): ExecutableScenario = ExecutableScenario(this, this.cls)

  private[jetprobe] def build(ctx: ScenarioContext,
                              chainNext: Action): Action = {
    actionBuilders.foldLeft(chainNext) { (next, actionBuilder) =>
      actionBuilder.build(ctx, next)
    }
  }

}

case class ExecutableScenario(scenarioBuilder: ScenarioBuilder, className: String, config: Map[String, Any] = Map.empty) {

  def build(system: ActorSystem,
            onExit: Action,
            controller: ActorRef): Scenario = {
    val ctx = ScenarioContext(system, controller)
    val entry = scenarioBuilder.build(ctx, onExit)
    Scenario(scenarioBuilder.name, entry, ctx, className, config)

  }
}

case class ScenarioContext(system: ActorSystem, controller: ActorRef)


trait Sample[T] {

  def getName(s: String): T = ???
}