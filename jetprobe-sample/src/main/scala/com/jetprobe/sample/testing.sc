import akka.actor.ActorSystem
import com.jetprobe.core.TestScenario
import com.jetprobe.core.runner.Runner
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.hadoop.sinks.HDFSSink
import com.jetprobe.hbase.sink.HBaseSink
import com.jetprobe.hbase.validation.HBaseValidationSupport
import org.json4s.JsonAST
import org.json4s.native.JsonMethods.parse

import scala.concurrent.duration._

class HelloSuite extends TestScenario with HBaseValidationSupport{

  case class FirstName(name : String,yob : Int)

  val clusterQuorum = "hdp26real02.informatica.com,hdp26real01.informatica.com,hdp26real03.informatica.com"
  val localCluster = "192.168.37.131"

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
      .validate[HBaseSink](hbase){

      given(hbase.select[FirstName]("select MDMBDRM_link_columns.NAME1,MDMBDRM_link_columns.YOB1 from MDMBDRM005_DSAB_Small_PK")
        ( s => parseNames(s) )){ result =>

        println(s"rows count : ${result.size}")
        assertEquals(100,result.size)

        assertEquals(2,result.count(p => p.name.contains("HAN")))

        assertEquals(50,result.count(p => p.yob > 1951))

      }
    }
      .build()

  }
}

implicit val actorSystem = ActorSystem("hola")

Runner().run(Seq(new HelloSuite().buildScenario))
