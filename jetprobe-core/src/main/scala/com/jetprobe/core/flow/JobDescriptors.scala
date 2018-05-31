package com.jetprobe.core.flow

import com.jetprobe.core.task.TaskMetrics
import com.jetprobe.core.validations.ValidationResult
import io.circe.{Decoder, HCursor}
import io.circe.generic.extras.{Configuration, semiauto => fancy}
//import io.circe.generic.extras.auto._

/**
  * @author Shad.
  */
object JobDescriptors {

  case class Pipeline(name: String,
                      description: String,
                      className: String,
                      exitOnFailure: Boolean = true,
                      repeat: Int = 1,
                      params: Map[String, String] = Map.empty)


  case class ScenarioMeta(name: String,
                          project: String, tags: Seq[String] = Seq.empty, pipelines: Seq[Pipeline], params: Map[String, String] = Map.empty)

  implicit val customConfig: Configuration =
    Configuration.default.withDefaults

  implicit val decodePipeline: Decoder[Pipeline] = fancy.deriveDecoder[Pipeline]
  implicit val decodeScenario: Decoder[ScenarioMeta] = fancy.deriveDecoder[ScenarioMeta]


  case class PipelineStats(id: String,
                           name: String,
                           className: String,
                           startTime: Long,
                           endTime: Long,
                           tasksStats: Array[TaskMetrics],
                           totalTasks: Int,
                           validationResults: Seq[ValidationResult])


  case class JobStats(project: String, scenarioMetrics: Map[String, Seq[PipelineStats]])

}
