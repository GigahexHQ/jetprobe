package com.jetprobe.core.http

import com.jetprobe.core.action.builder.HttpRequestActionBuilder
import com.jetprobe.core.structure.Execs

import scala.io.Source

/**
  * @author Shad.
  */
trait HttpDSL[B] extends Execs[B] {

  def http(requestBuilder: HttpRequestBuilder) : B = exec(new HttpRequestActionBuilder(requestBuilder))


}
