package com.jetprobe.consul.model

import com.jetprobe.core.sink.DataSource

/**
  * @author Shad.
  */
case class ServiceInfo(name : String, host : String, port : Int) extends DataSource

class ServiceQuery(val name : String)

class ServicesQuery(val names : String*)

