package com.jetprobe.mongo.validation

import com.jetprobe.core.parser._
import org.bson.conversions.Bson
import org.mongodb.scala.model.Filters


/**
  * @author Shad.
  */
object BinaryOperationHandler {

  def handleBinaryOperation(field : SqlExpr, value : SqlExpr, createFilter : (String,LiteralExpr) => Bson) : Bson = {

    field match {
      case FieldIdent(_,name,_,_) =>
        value match {
          case lit : LiteralExpr => createFilter(name,lit)
          case _ => throw new IllegalArgumentException("Operand must be a literal of type Int, Float or String")
        }
      case _ => throw new IllegalArgumentException("Supported Operators :  [>=, =, <=, >, <] ")
    }

  }

  def handleLessThan(field : String, literalExpr: LiteralExpr) : Bson = {
    Filters.lt(field,getOperandValue(literalExpr))
  }

  def handleLessThanEqual(field : String, literalExpr: LiteralExpr) : Bson = {
    Filters.lte(field,getOperandValue(literalExpr))
  }

  def handleLEqual(field : String, literalExpr: LiteralExpr) : Bson = {
    Filters.eq(field,getOperandValue(literalExpr))
  }

  def handleGreaterThan(field : String, literalExpr: LiteralExpr) : Bson = {
    Filters.gt(field,getOperandValue(literalExpr))
  }

  def handleGreaterThanEqual(field : String, literalExpr: LiteralExpr) : Bson = {
    Filters.gte(field,getOperandValue(literalExpr))
  }

  def getOperandValue(literal : LiteralExpr) : Any = {
    literal match {
      case IntLiteral(x,_) => x
      case FloatLiteral(x,_) => x
      case StringLiteral(x,_) => x
    }
  }




}
