package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.{ActionBuilder, IngestionBuilder}
import com.jetprobe.core.generator.DataGenerator


/**
  * @author Shad.
  */
trait DataFeed[B] extends Execs[B]{

  def doWith(dataset : DataGenerator)(writer : Iterator[String] => Unit) : B = exec(new IngestionBuilder(dataset,writer))


}
