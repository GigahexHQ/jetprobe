package com.jetprobe.core.runner

import akka.actor.ActorSystem
import com.jetprobe.core.action.Exit
import com.jetprobe.core.controller.Controller
import com.jetprobe.core.controller.ControllerCommand.{ScheduleTestSuites, Start}
import com.jetprobe.core.structure.{ExecutablePipeline, PipelineBuilder}
import com.typesafe.scalalogging.StrictLogging


/**
  * @author Shad.
  */
class Runner(system: ActorSystem,hasReport : Boolean) extends StrictLogging {

  def run(testSuites: ExecutablePipeline*): Unit = {

    val controller = system.actorOf(Controller.props(hasReport))
    val onExit = new Exit(controller)
    controller ! ScheduleTestSuites(testSuites)

  }

}

object Runner {

  def apply(report : Boolean = true)(implicit system: ActorSystem) = new Runner(system,report)
}
