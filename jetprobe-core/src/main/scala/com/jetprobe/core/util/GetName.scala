package com.jetprobe.core.util

/**
  * @author Shad.
  */
import java.util.concurrent.atomic.AtomicLong

object GetName {
  val IdGen = new AtomicLong
}

trait GetName {
  import GetName._

  def genName(base: String) = base + "-" + IdGen.incrementAndGet
}
