package com.jetprobe.hbase.validation

import com.jetprobe.core.parser._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter.{Filter, SingleColumnValueFilter}
import org.apache.hadoop.hbase.util.Bytes

/**
  * @author Shad.
  */
object BinaryOperationHandler {

  def handleBinaryOperation(field : SqlExpr, value : SqlExpr, createFilter : (Array[Byte],Array[Byte],LiteralExpr) => Filter) : Filter= {

    field match {
      case FieldIdent(_,name,_,_) =>
        val (cf,column) = HBaseQueryBuilder.getHBaseColumnMeta(name)
        value match {
          case lit : LiteralExpr => createFilter(cf,column,lit)
          case _ => throw new IllegalArgumentException("Operand must be a literal of type Int, Float or String")
        }
      case _ => throw new IllegalArgumentException("Supported Operators :  [>=, =, <=, >, <] ")
    }


  }

  def handleGreaterThanEqual(cf : Array[Byte], column : Array[Byte], literal : LiteralExpr) : Filter = {
    new SingleColumnValueFilter(cf,column,CompareOp.GREATER_OR_EQUAL,getOperandValue(literal))
  }

  def handleGreaterThan(cf : Array[Byte], column : Array[Byte], literal : LiteralExpr) : Filter = {
    new SingleColumnValueFilter(cf,column,CompareOp.GREATER,getOperandValue(literal))
  }

  def handleLessThan(cf : Array[Byte], column : Array[Byte], literal : LiteralExpr) : Filter = {
    new SingleColumnValueFilter(cf,column,CompareOp.LESS,getOperandValue(literal))
  }

  def handleLessThanEqual(cf : Array[Byte], column : Array[Byte], literal : LiteralExpr) : Filter = {
    new SingleColumnValueFilter(cf,column,CompareOp.LESS_OR_EQUAL,getOperandValue(literal))
  }

  def handleEqual(cf : Array[Byte], column : Array[Byte], literal : LiteralExpr) : Filter = {
    new SingleColumnValueFilter(cf,column,CompareOp.EQUAL,getOperandValue(literal))
  }


  def getOperandValue(literal : LiteralExpr) : Array[Byte] = {
    literal match {
      case IntLiteral(x,_) => Bytes.toBytes(x)
      case FloatLiteral(x,_) => Bytes.toBytes(x)
      case StringLiteral(x,_) => Bytes.toBytes(x)
    }
  }

}
