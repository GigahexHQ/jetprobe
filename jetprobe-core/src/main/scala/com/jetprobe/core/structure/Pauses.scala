package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.PauseActionBuilder

import scala.concurrent.duration.FiniteDuration

/**
  * @author Shad.
  */
trait Pauses[B] extends Execs[B]{

  def pause(duration : FiniteDuration) : B = exec(new PauseActionBuilder(duration))

}
