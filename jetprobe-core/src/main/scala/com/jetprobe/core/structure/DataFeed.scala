package com.jetprobe.core.structure

import com.jetprobe.core.task.builder.{TaskBuilder, IngestionBuilder, StorageIOBuilder}
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.storage.Storage


/**
  * @author Shad.
  */
trait DataFeed[B] extends Execs[B]{

  def doWith(dataset : DataGenerator)(writer : Iterator[String] => Unit) : B = exec(new IngestionBuilder(dataset,writer))

  def doWith[S <: Storage](generator: DataGenerator, config : Config[S])(fnRunner : (Iterator[String],S) => Unit) =
    exec(new StorageIOBuilder[S](generator,config,fnRunner))

}
