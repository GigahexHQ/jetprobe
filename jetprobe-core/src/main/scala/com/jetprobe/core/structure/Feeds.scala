package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.FeedBuilder
import com.jetprobe.core.generator.DataGenerator

/**
  * @author Shad.
  */
trait Feeds[B] extends Execs[B] {

  def feed(feeder: DataGenerator): B = exec(new FeedBuilder(feeder))

}
