package com.jetprobe.core.storage

import com.jetprobe.core.task.builder.TaskBuilder
import com.jetprobe.core.generator.Generator
import com.jetprobe.core.validations.ValidationRule
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

/**
  * @author Shad.
  */
trait DataSource
trait Storage extends LazyLogging with DataSource {



  def cleanup : Unit = logger.info("Cleaning up resources")

  def isExpr(str : String) : Boolean = {

    val stck = new mutable.Stack[Char]()
    for {
      i <- str.indices

    } yield {
      val ch = str.charAt(i)
      if(ch.equals('$') || ch.equals('{'))
        stck.push(ch)
      else if(ch.equals('}') && stck.nonEmpty && stck.size != 1){
        stck.pop()
      }
    }

    (stck.nonEmpty && stck.size == 1 && stck.pop().equals('$'))
  }


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


