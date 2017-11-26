package com.jetprobe.core


import com.jetprobe.core.structure.ExecutableScenario

/**
  * @author Shad.
  */
trait TestScenario extends CoreDsl{

  def buildScenario : ExecutableScenario

}

abstract class JTestScenario extends TestScenario