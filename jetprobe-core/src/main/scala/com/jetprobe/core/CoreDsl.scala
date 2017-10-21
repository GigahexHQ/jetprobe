package com.jetprobe.core

import com.jetprobe.core.sink.SinkSupport
import com.jetprobe.core.generator.DataGeneratorSupport
import com.jetprobe.core.http.HttpSupport
import com.jetprobe.core.structure.ScenarioBuilder
import org.mongodb.scala.MongoClient

/**
  * @author Shad.
  */
trait CoreDsl extends DataGeneratorSupport with SinkSupport with HttpSupport{

  def scenario(scenarioName: String): ScenarioBuilder = ScenarioBuilder(scenarioName)
  //def assert[S](f : S => Boolean) : S => Boolean = f

}

object Predef extends CoreDsl {
  type Session = com.jetprobe.core.session.Session

}
