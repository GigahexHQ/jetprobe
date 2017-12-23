package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.hadoop.sinks.HDFSSink

/**
  * @author Shad.
  */
@TestSuite
class HDFSSuite extends TestScenario {

  val hdfs = HDFSSink("hdfs://10.75.141.203")

  override def buildScenario: ExecutableScenario = {

    scenario("hdfs test suite")
      .exec(

        //Copy the file
        hdfs.copy(localPath = """C:\Users\samez\Documents\relate360\data\deals_from_hdfs.in""", hdfsDir = "/user/r360/interesting_deals.in"),
        //Create the directory
        hdfs.mkdir("/user/root/shad/data"),

        hdfs.download("/user/r360/deals/deals.in0","""C:\Users\samez\Documents\relate360\data\deals_from_hdfs.in"""),

        hdfs.download("/user/r360/deals/deals.in","""C:\Users\samez\Documents\relate360\data\deals_copied.in""")

        //.exec(hdfs.delete("/user/cloudera/r360/deal.in"))
      )
      .build()

  }
}
