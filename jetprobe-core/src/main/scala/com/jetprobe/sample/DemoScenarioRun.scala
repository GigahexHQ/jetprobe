package com.jetprobe.sample

import akka.actor.ActorSystem
import com.jetprobe.core.extractor.JsonPathExtractor._
import com.jetprobe.core.http.Http
import com.jetprobe.core.runner.Runner

import scala.concurrent.duration._

/**
  * @author Shad.
  */
object DemoScenarioRun extends App {

  import com.jetprobe.core.Predef._



  private def debug2(name: String)(value: sourcecode.Text[Int => Int]) = {
    println(" [" + value.source + "]: " + value.value)
  }


  val dataset = fromTemplate(
    "C:\\Users\\samez\\Documents\\match-service\\sample.txt",
    "C:\\Users\\samez\\Documents\\match-service\\data.csv",
    100)

  val defaultDB = "52ZSTB0IDK6dXxaEQLUaQu"
  val coll = "PfObjects"

  //val mongo = mongodb("mongodb://192.168.244.130/hHTk8DHiIKhlTXuU4ewN8V/xref.customer")

  val getPosts = Http("getPosts")
    .get("https://jsonplaceholder.typicode.com/posts/1")
    .extract(
      jsonPath("$.userId", saveAs = "userId"),
      jsonPath("$.title", saveAs = "title")
    )

  val insertPost = Http("insertionPost")
    .post("https://reqres.in/api/users")
    .body(
      fromFile("C:\\Users\\samez\\Documents\\match-service\\post_req.json")
    )
    .header("Content-type", "application/json")
    .extract(
      jsonPath("$.name", saveAs = "username"),
      jsonPath("$.id", saveAs = "id")
    )


  val pipe = scenario("First Test Scenario")
    .http(insertPost)
    .pause(3.seconds)
    .http(getPosts)
    //.finish
  /*.validate(mongo) { mng =>

    mng.forServer(
      checkStats[String]("3.4.0", _.version),
      checkStats[Boolean](true, _.version.startsWith("3.4")),
      checkStats[Boolean](true, _.connections.current < 50),
      checkStats[Long](80L, _.opcounters.insert)
    )

    mng.forServer(
      checkStats[Boolean](false, _.version != "3.2.0")
    )

    mng.forServer(
      checkDatabaseList[Int](3, _.databases.length),
      checkDatabaseList[Boolean](true, _.databases.exists(_.name == "52ZSTB0IDK6dXxaEQLUaQu"))
    )

    mng.forDatabase(db = "52ZSTB0IDK6dXxaEQLUaQu",
      checkDBStats[Int](2, dbStats => dbStats.collections),
      checkDBStats[Double](48.5D, _.indexSize),
      checkDBStats[Int](108, _.objects)
    )

    mng.forDatabaseAndCollection(database = "52ZSTB0IDK6dXxaEQLUaQu", coll = "PfObjects",
      checkCollectionStats[Boolean](true, _.ns.startsWith("52Z")),
      checkCollectionStats[Int](30, _.totalIndexSize)
    )

    mng.forDatabaseAndCollection(database = "52ZSTB0IDK6dXxaEQLUaQu", coll = "PfObjects",
      checkDocuments[Int](query = "{status : \"Active\"}",105, _.count)
    )
  }
  .pause(1.seconds)

  //Add some more tests
  .finish

implicit val actorSystem = ActorSystem()


Runner().run(pipe)
//System.exit(0)
*/
}
