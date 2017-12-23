package com.jetprobe.core.runner

import akka.actor.ActorSystem
import com.jetprobe.core.action.Exit
import com.jetprobe.core.controller.Controller
import com.jetprobe.core.controller.ControllerCommand.{ScheduleTestSuites, Start}
import com.jetprobe.core.structure.ExecutableScenario
import com.typesafe.scalalogging.StrictLogging


/**
  * @author Shad.
  */
class Runner(system: ActorSystem,hasReport : Boolean) extends StrictLogging {

  def run(testSuites: Seq[ExecutableScenario]): Unit = {
    //logger.trace(s"Starting the execution of the scenario : ${scenario.name}")

    val controller = system.actorOf(Controller.props(hasReport))
    val onExit = new Exit(controller)
    //val scenarios = testSuites.map(_.build(system,onExit,controller))
    //controller ! Start(scenarios)
    controller ! ScheduleTestSuites(testSuites)

  }

}

object Runner {

  def apply(report : Boolean = false)(implicit system: ActorSystem) = new Runner(system,report)
}
