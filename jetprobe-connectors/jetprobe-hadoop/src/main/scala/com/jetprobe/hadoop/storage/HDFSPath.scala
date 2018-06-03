package com.jetprobe.hadoop.storage

import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult}

/**
  * @author Shad.
  */
case class HDFSPath(path : String, storage : HDFSStorage) extends ValidationExecutor[HDFSStorage]{

  def hasLines(count : Int)(whereCondn : String => Boolean) : ValidationResult = {
    assertThat(count,storage)(hdfs => hdfs.readLines(path).count(whereCondn))
  }

  def totalLineCount(count : Int) : ValidationResult = {
    assertThat(count,storage)(hdfs => hdfs.readLines(path).size)
  }

  def hasLines(whereCondn : String => Boolean) : ValidationResult = {
    assertThat(true,storage)(hdfs => hdfs.readLines(path).size > 0)
  }

  def nonEmpty : ValidationResult = {
    hasLines(s => true)
  }

  def isEmpty : ValidationResult = {
    hasLines(s => false)
  }

  def contains(string : String) : ValidationResult = {
    assertThat(true,storage)(hdfs => hdfs.readLines(path).count(_.contains(string)) > 0 )
  }

  def ==(lines : Array[String]) : ValidationResult = {
    assertThat(lines,storage)(hdfs => hdfs.readLines(path).toArray)
  }



}
