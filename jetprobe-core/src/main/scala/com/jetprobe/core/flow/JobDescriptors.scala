package com.jetprobe.core.flow

import com.jetprobe.core.task.TaskMetrics
import io.circe.{Decoder, HCursor}
import io.circe.generic.extras.{Configuration, semiauto => fancy}
//import io.circe.generic.extras.auto._

/**
  * @author Shad.
  */
object JobDescriptors {

  case class Pipeline(name : String, description : String,className : String, exitOnFailure : Boolean = true, repeat : Int = 1,params : Map[String,String] = Map.empty)


  /*implicit val decodePipeline: Decoder[Pipeline] = new Decoder[Pipeline] {
    final def apply(c: HCursor): Decoder.Result[Pipeline] =
      for {
        name <- c.downField("name").as[String]
        description <- c.downField("description").as[String]
        className <- c.downField("className").as[String]
        exitOnFailure <- c.downField("exitOnFailure").as[Boolean]
        repeat <- c.downField("repeat").as[Int]
        params <- c.downField("params").as[Map[String,String]]
      } yield {
        Pipeline(name,description,className,exitOnFailure,repeat,params)
      }
  }*/

  case class ScenarioMeta(name : String, project : String, tags : Seq[String] = Seq.empty,pipelines : Seq[Pipeline], params : Map[String,String] = Map.empty)

  implicit val customConfig: Configuration =
    Configuration.default.withDefaults

  implicit val decodePipeline: Decoder[Pipeline] = fancy.deriveDecoder[Pipeline]
  implicit val decodeScenario: Decoder[ScenarioMeta] = fancy.deriveDecoder[ScenarioMeta]

  /*implicit val decodeScenario: Decoder[ScenarioMeta] = new Decoder[ScenarioMeta] {
    final def apply(c: HCursor): Decoder.Result[ScenarioMeta] =
      for {
        name <- c.downField("name").as[String]
        project <- c.downField("project").as[String]
        tags <- c.downField("tags").as[Seq[String]]
        pipes <- c.downField("pipelines").as[Seq[Pipeline]]
        params <- c.downField("params").as[Map[String,String]]
      } yield {
        ScenarioMeta(name,project,tags,pipes,params)
      }
  }*/


  case class PipelineStats(id : String, name : String, startTime : Long, endTime : Long, tasksStats : Array[TaskMetrics])

}
