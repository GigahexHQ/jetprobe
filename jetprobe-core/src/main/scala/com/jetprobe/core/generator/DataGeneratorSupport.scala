package com.jetprobe.core.generator

/**
  * @author Shad.
  */
trait DataGeneratorSupport {

  def fromTemplate(path: String,
                   datasetPath: String,
                   recordCount: Int): DataGenerator =
    RandomGen(datasetPath, path, recordCount)

}
