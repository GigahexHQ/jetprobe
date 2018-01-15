package com.jetprobe.mongo.validation

import com.jetprobe.core.parser._
import org.bson.conversions.Bson

/**
  * @author Shad.
  */
object QueryBuilder {

  import BinaryOperationHandler._

  def buildFilter(sqlStmt : SelectStmt) : Option[Bson] = {

    sqlStmt.filter map {
      case Ge(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleGreaterThanEqual)
      case Gt(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleGreaterThan)
      case Lt(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleLessThan)
      case Le(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleLessThanEqual)
      case Eq(lhs,rhs,ctx) => handleBinaryOperation(lhs,rhs,handleLEqual)
      case _ => throw new IllegalArgumentException("Operator not supported")

    }
  }

  def getProjections(projs : Seq[SqlProj]) : Seq[String] = {
    SQLParser.getColumnMeta(projs).map(_.fieldName)
  }

  def getTable(sqlStmt : SelectStmt) : (String,String) = {
    sqlStmt.relations match {
      case Some(xs) => xs.head match {
        case TableRelationAST(name,_,_) =>
          val schema = name.split("\\.")
          if(schema.size < 2)
            throw new IllegalArgumentException("Expected format for the table : <database>.<collection>")
          else
            (schema(0),schema(1))

        case _ => throw new IllegalArgumentException("Missing table in the sql query")
      }
      case None => throw new IllegalArgumentException("Missing table in the sql query")
    }

  }

}
