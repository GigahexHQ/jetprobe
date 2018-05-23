package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.fs.File
import com.jetprobe.core.structure.PipelineBuilder

/**
  * @author Shad.
  */
@PipelineMeta(name = "Files testing")
class FileSuite extends TestPipeline {

  /**
    * Define the list of task that needs to be executed as part of the test suite
    *
    * @return The Scenario consisting of list of tasks
    */
  override def tasks: PipelineBuilder = {

    validate("sometest", File.at("""/path/to/file""")) {
      file =>
        file.hasTotalLines(200)

        assertTrue(!file.underLyingFile.isDirectory)

        file.hasLines(120) { line =>
          line.contains("mapreduce.Job")
        }

    }
  }
}
