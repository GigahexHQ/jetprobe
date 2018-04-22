package com.jetprobe.core.generator

import java.util.UUID

/**
  * @author Shad.
  */
object ActorNameGenerator {

  def getName(name : String) : String = {
    name.replaceAll(" ","-") + UUID.randomUUID().toString
  }

}
