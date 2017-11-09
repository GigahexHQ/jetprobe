package com.jetprobe.core.sink

import com.jetprobe.core.generator.Generator
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
trait DataSource
trait DataSink extends LazyLogging with DataSource{

  
  def save(record : Generator) : Unit = ???

}

trait SinkSupport {

  val console = new ConsoleSink

}

class ConsoleSink extends DataSink {

  override def save(records: Generator) = while(records.hasNext) println(records.next())

}

