package com.jetprobe.core.structure

import com.jetprobe.core.task.ExecutableTask

/**
  * @author Shad.
  */
case class Scenario(name : String, entry : ExecutableTask, ctx : PipelineContext, className : String, configAttr : Map[String,Any])
