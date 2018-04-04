package com.jetprobe.core.structure

import com.jetprobe.core.task.Task

/**
  * @author Shad.
  */
case class Scenario(name : String, entry : Task, ctx : PipelineContext, className : String, configAttr : Map[String,Any])
