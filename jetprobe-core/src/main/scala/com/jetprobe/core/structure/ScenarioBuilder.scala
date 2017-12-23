package com.jetprobe.core.structure

import akka.actor.{ActorRef, ActorSystem}
import com.jetprobe.core.action.Action
import com.jetprobe.core.action.builder.ActionBuilder

/**
  * @author Shad.
  */
case class ScenarioBuilder(name: String,
                           actionBuilders: List[ActionBuilder] = Nil)
    extends StructureBuilder[ScenarioBuilder] {

  private[core] def newInstance(actionBuilders: List[ActionBuilder]) =
    copy(actionBuilders = actionBuilders)

  //def build: ExecutableScenario = ExecutableScenario(this)
  override def exec(actionBuilder: ActionBuilder): ScenarioBuilder = this.copy(actionBuilders = List(actionBuilder) ::: actionBuilders)

  def build(): ExecutableScenario = ExecutableScenario(this)

}

case class ExecutableScenario(scenarioBuilder: ScenarioBuilder, className : String= "", config : Map[String,Any] = Map.empty) {

  def build(system: ActorSystem,
            onExit: Action,
            controller: ActorRef): Scenario = {
    val ctx = ScenarioContext(system, controller)
    val entry = scenarioBuilder.build(ctx, onExit)
    Scenario(scenarioBuilder.name, entry, ctx,className,config)

  }
}

case class ScenarioContext(system: ActorSystem, controller: ActorRef)


trait Sample[T] {

  def getName(s : String) : T = ???
}