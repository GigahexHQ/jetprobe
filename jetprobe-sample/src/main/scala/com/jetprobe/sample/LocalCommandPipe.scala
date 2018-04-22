package com.jetprobe.sample

import akka.actor.ActorSystem
import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.runner.Runner
import com.jetprobe.core.structure.PipelineBuilder

/**
  * @author Shad.
  */

@PipelineMeta(name = "Local command")
class LocalCommandPipe extends TestPipeline{
  /**
    * Define the list of task that needs to be executed as part of the test suite
    *
    * @return The Scenario consisting of list of tasks
    */
  override def tasks: PipelineBuilder = {

    runCmd("get maven version","mvn --version")

    //show the node version
    runCmd("yarn start","""C:\Users\samez\Documents\projects\jetprobe-gitlab\jetprobe-ui""")

    //runCmd("mvn install -DskipTests",at = """C:\Users\samez\Perforce\match-nxt\code\match""")

  }
}

object LocalCommandPipeRun extends App {

  implicit val system = ActorSystem("local-cmd")

  Runner().run(new LocalCommandPipe().tasks.build())

}