package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.generator.TemplateDataGen
import com.jetprobe.core.structure.{ExecutablePipeline, PipelineBuilder}
import com.jetprobe.hadoop.storage.{HDFSConfig, HDFSStorage}
import org.apache.hadoop.fs.Path

/**
  * @author Shad.
  */
@PipelineMeta(name = "HDFS Testing")
class HDFSSuite extends TestPipeline {

  val clusterHDFS = "hdfs://xxx.xx.xx"

  val hdfsConf = new HDFSConfig(clusterHDFS,"shad")

  val datagen = new TemplateDataGen(
    """/path/to/template.in""",
    """path/to/dataset.out""", 1000)

  override def tasks: PipelineBuilder= {

      task("Copy sample data",datagen,hdfsConf){ (data,hdfs) =>

        hdfs.mkdir("/user/shad/data")
        hdfs.write(data,"/user/shad/data/sample.in")

      }

      task("Cp sample directories",hdfsConf){ hadoop =>

        //Copy the file
        hadoop.copyFromLocal(localSrc= """/local/source/path""", destination = "/destination/path.out")

        hadoop.copyToLocal("/path/to/hdfs","""/path/to/local""")

      }

    runCmd("local cmd","ls -al")

    validate("HDFS validate",hdfsConf){ hdfs =>

      given(hdfs.usingFS(fs => fs.getFileStatus(new Path("/user/name/data.in")))) { status =>
        assertEquals("hdfs",status.getOwner)

      }
    }

    validate("check file output",hdfsConf) { hdfs =>

      val fsStatus = hdfs.usingFS(fs => fs.getContentSummary(new Path("/user/name/path")))

      assertEquals(24L,fsStatus.get.getFileCount)

    }
  }
}
