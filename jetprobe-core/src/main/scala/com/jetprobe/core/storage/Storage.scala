package com.jetprobe.core.storage

import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.generator.Generator
import com.jetprobe.core.validations.ValidationRule
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
trait DataSource
trait Storage extends LazyLogging with DataSource {



  def cleanup : Unit = logger.info("Cleaning up resources")


}

trait StorageQuery[S <: Storage,T] {

  def build(fn : Seq[T] => Any,line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationRule[S]

}

/*
trait StorageQueryBuilder[S <:Storage] {

  /**
    * Validation Rule builder
    * @param query An expression that would define the rule to access the stored data
    * @param fnRule
    * @tparam D
    * @return
    */



}
*/


