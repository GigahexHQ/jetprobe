package com.jetprobe.core.util

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * @author Shad.
  */
object FileReader {


  /**
    * Read entire file and concat into 1 line
    *
    * @param f
    * @return
    */
  def readFile(f: File): String = {
    val dataset = new ArrayBuffer[String]()
    val bs = Source.fromFile(f)
    for (data <- bs.getLines) {
      dataset += data
    }
    bs.close
    dataset.mkString("").split('\n').map(_.trim.filter(_ >= ' ')).mkString

  }

}