package com.jetprobe.core.generator

import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
trait DataGenerator {

  def build(ctx : ScenarioContext) : Generator

}
