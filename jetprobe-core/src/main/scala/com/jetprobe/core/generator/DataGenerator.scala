package com.jetprobe.core.generator

import com.jetprobe.core.Predef.Session
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
trait DataGenerator {

  def generate(session: Session) : Option[Iterator[String]]


}
