package com.jetprobe.core.parser

import com.jetprobe.core.storage.Storage

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Shad.
  */
trait SqlExecutor {

  def execute[T,S <: Storage](sql : String, config : Map[String,Any],storage : S, executorFn : (SelectStmt,S) => Seq[String], converterFn : String => T) : Future[Either[Throwable,Seq[T]]] = {

    val queryResult = Try {
      ExpressionParser.parse(sql,config).flatMap { sql =>
        val sqlParser = new SQLParser
        sqlParser.parse(sql)
      }.map(stmt => executorFn(stmt,storage))
        .map(xs => xs.map(converterFn(_)))
    }

    val futureResult = queryResult match {
      case Success(optResult) => optResult match {
        case Some(result) => Right(result)
        case None => Left(new Exception(s"Unable to execute SQL : ${sql}"))
      }
      case Failure(ex) => Left(ex)

    }

    Future(futureResult)

  }

}
