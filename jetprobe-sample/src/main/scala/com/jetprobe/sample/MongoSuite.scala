package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.structure.PipelineBuilder
import com.jetprobe.mongo.storage.MongoDBConf
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * @author Shad.
  */
@PipelineMeta(name = "MongoDB TestSuite")
class MongoSuite extends TestPipeline with LazyLogging {


  val mongoConf = new MongoDBConf("mongodb://${mongo.host.name}")

  case class Person(_id: Long, name: String, age: Int)

  implicit val converter = (x: String) => parse(x).extract[Person]

  override def tasks: PipelineBuilder = {


    task("Create Mongo Collection",mongoConf) { mongoDB =>

      mongoDB.createCollection(db= "customer", collection = "purchase",indexFields = Seq("name","product"))
      mongoDB.createCollection("zoo", "baz")

    }


    validate("Collection Indices testing",mongoConf){ mongodb =>

      val info = mongodb.getDatabaseStats("foo").get
      assertEquals(1,info.get("indexes").get.asInt32().getValue)

    }


  }

}

