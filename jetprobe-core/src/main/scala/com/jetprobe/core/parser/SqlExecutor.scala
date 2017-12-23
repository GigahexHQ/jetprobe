package com.jetprobe.core.parser

/**
  * @author Shad.
  */
trait SqlExecutor {

  def execute[T](sql : String, config : Map[String,Any], executorFn : (SelectStmt) => Seq[String], converterFn : String => T) : Either[Throwable,Seq[T]] = {

    ???

  }

}
