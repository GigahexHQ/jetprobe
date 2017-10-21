package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.structure.ExecutableScenario
import scala.concurrent.duration._

/**
  * @author Shad.
  */
class AnotherScn extends TestScenario{

  override def buildScenario: ExecutableScenario = scenario("Second").pause(4.seconds).build

}
