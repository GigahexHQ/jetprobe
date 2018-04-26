package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.task.SSHConfig
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.structure.{ExecutablePipeline, PipelineBuilder}
import com.jetprobe.hbase.storage.HBaseConfig
import org.json4s.{JsonAST, _}
import org.json4s.native.JsonMethods._

/**
  * @author Shad.
  */
@PipelineMeta(name = "HBase Test Suite")
class HBaseSuite extends TestPipeline {

  case class FirstName(name: String, yob: Int)

  case class Employee(name: String, salary: Int)

  val clusterHost = "xxx.xx.xx.xx"

  val sshConf = SSHConfig(clusterHost, "username", "password")

  val empQuery = "select info.name, cast(info.salary,INT) from employee where data.salary > 2500 "

  val hbaseConf = new HBaseConfig(
    Map(
      "hbase.zookeeper.property.clientPort" -> "2181",
      "hbase.zookeeper.quorum" -> clusterHost,
      "zookeeper.znode.parent" -> "/${hbase.znode}"
    )
  )


  def parseNames(json: String): FirstName = {
    val parsed = parse(json)
    val result = parsed transformField {
      case ("yob", JString(x)) => ("yob", JInt(x.toInt))
    }

    result.extract[FirstName]
  }

  override def tasks: PipelineBuilder = {

    ssh("Run some commands",sshConf) { client =>

      client.run("ls -lrt /data/relate")

      client.upload("/from/file/path","/to/target/file/path")

      client.download("","")



    }

    task("HBase DML Commands",hbaseConf) { hbase =>

      //create the namespace
      hbase.createNamespace("infa")

      //first drop the table
      hbase.dropTable("employee","data")

      hbase.createTable("indexes","user",Seq("fields","data"))

      //create the table with column families
      hbase.createTable("employee", "infa", Seq("data","department"))


    }

    validate("check if tables are created",hbaseConf) { hbase =>

      given(hbase.describe("infa:employee")) { desc =>

        assertEquals(1, desc.getColumnFamilies.size)
        assertEquals("",desc.getFlushPolicyClassName)

      }

    }

    validate("Test HBase running status",hbaseConf) { hbase =>

      given(hbase.status) { value =>
        assertEquals(0, value.deadServers)
        assertEquals("some-id-random",value.clusterId)
      }
    }

    validate("HBase query test",hbaseConf) { hbase =>

      val query = hbase.select[Employee](empQuery, s => parse(s).extract[Employee])

      given(query) { result =>

        val array = result.toArray
        assertEquals(1000, result(7).salary)

      }
    }

  }

}
