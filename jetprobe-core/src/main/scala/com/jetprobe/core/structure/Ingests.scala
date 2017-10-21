package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.IngestionBuilder
import com.jetprobe.core.sink.DataSink

/**
  * @author Shad.
  */
trait Ingests[B] extends Execs[B]{

  def into (sink : DataSink) : B = exec(new IngestionBuilder(sink))

}
