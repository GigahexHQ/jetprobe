package com.jetprobe.core.structure

import com.jetprobe.core.action.Action

/**
  * @author Shad.
  */
case class Scenario(name : String, entry : Action, ctx : PipelineContext, className : String, configAttr : Map[String,Any])
