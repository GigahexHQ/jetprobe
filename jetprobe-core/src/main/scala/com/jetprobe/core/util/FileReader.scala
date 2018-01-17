package com.jetprobe.core.util

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * @author Shad.
  */
object FileReader {

  /**
    * Populates the array with the sample data
    *
    * @param filePath
    * @return the dataset
    */
  def getDataSet(filePath: String): Array[String] = {
    val dataset = new ArrayBuffer[String]()
    val fileResource = getClass.getResource("/" + filePath)
    val bufferedSource = if (fileResource != null) {
      val stream = getClass.getResourceAsStream("/" + filePath)
      Source.fromInputStream(stream)
    } else
      Source.fromFile(new File(filePath))

    for (data <- bufferedSource.getLines) {
      dataset += data
    }
    bufferedSource.close
    dataset.toArray
  }

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
    dataset.mkString("")

  }

}