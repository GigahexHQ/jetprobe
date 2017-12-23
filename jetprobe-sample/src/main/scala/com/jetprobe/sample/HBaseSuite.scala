package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.hbase.sink.HBaseSink
import com.jetprobe.hbase.validation.HBaseValidationSupport
import org.json4s.{JsonAST, _}
import org.json4s.native.JsonMethods._

/**
  * @author Shad.
  */
@TestSuite
class HBaseSuite extends TestScenario with HBaseValidationSupport{

  case class FirstName(name : String,yob : Int)
  case class Employee(name : String, salary : Int)

  val clusterQuorum = "hdp26real02.informatica.com,hdp26real01.informatica.com,hdp26real03.informatica.com"
  val localCluster = "192.168.37.131"

  val bdrmQuery = "select MDMBDRM_link_columns.NAME1,MDMBDRM_link_columns.YOB1 from MDMBDRM005_DSAB_Small_PK where MDMBDRM_link_columns.YOB1 > 1951"

  val empQuery = "select info.name,info.salary from emp where info.salary > 2500 "

  val hbase = HBaseSink(
    Map(
    "hbase.zookeeper.property.clientPort" -> "2181",
    "hbase.zookeeper.quorum" -> localCluster,
      "zookeeper.znode.parent" -> "/hbase"
  )
  )

  def parseNames(json : String) : FirstName = {
    val parsed = parse(json)
    val result =  parsed transformField {
      case ("NAME1",x) => ("name",x)
      case ("YOB1",JsonAST.JString(v)) => ("yob",JsonAST.JInt(v.toInt))
    }

    result.extract[FirstName]
  }

  override def buildScenario: ExecutableScenario = {

    scenario("HBase table validation")
      .validate[HBaseSink](hbase)(

        given(hbase.select[FirstName](bdrmQuery)
          ( s => parseNames(s) )){ result =>

          println(s"rows count : ${result.size}")
          assertEquals(64,result.size)

          assertEquals(2,result.count(p => p.name.contains("HAN")))

          // assertEquals(50,result.count(p => p.yob > 1951))

        },
        given(hbase.select[Employee](empQuery)
          ( s => parse(s).extract[Employee])){ result =>

          println(s"rows count : ${result.size}")
          assertEquals(2,result.size)

          //assertEquals(2,result.count(p => p.name.contains("HAN")))

          // assertEquals(50,result.count(p => p.yob > 1951))

        }
    )
      .build()

  }

}
