package com.jetprobe.core.structure

import com.jetprobe.core.task.builder.{TaskBuilder, IngestionBuilder, StorageIOBuilder}
import com.jetprobe.core.generator.DataGenerator
import com.jetprobe.core.storage.Storage


/**
  * @author Shad.
  */
trait DataFeed[B] extends Execs[B] {

  def task(dataset: DataGenerator)(description: String, writer: Iterator[String] => Unit): B = exec(new IngestionBuilder(description, dataset, writer))

  def task[S <: Storage](description: String, generator: DataGenerator, config: Config[S])(fnRunner: (Iterator[String], S) => Unit) =
    exec(new StorageIOBuilder[S](description, generator, config, fnRunner))

}
