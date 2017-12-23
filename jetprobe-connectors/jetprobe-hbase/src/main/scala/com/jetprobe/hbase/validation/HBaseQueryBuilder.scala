package com.jetprobe.hbase.validation

import com.jetprobe.core.parser._
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.util.Bytes

/**
  * @author Shad.
  */
object HBaseQueryBuilder {

  import BinaryOperationHandler._

  private val allFields = "*"
  private val maxBatchSize = 100
  private val cacheSize = 1000

  def parseQuery(scan: Scan, selectStmt: SelectStmt): Scan = {

    scan.setCaching(cacheSize)
    val updatedScan = parseProjections(selectStmt.projections, scan)
    parseFilters(selectStmt, updatedScan)

  }

  private def parseFilters(selectStmt: SelectStmt, scan: Scan): Scan = {
    val filter = selectStmt.filter map {
      case Ge(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleGreaterThanEqual)
      case Gt(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleGreaterThan)
      case Lt(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleLessThan)
      case Le(lhs, rhs, ctx) => handleBinaryOperation(lhs, rhs, handleLessThanEqual)
      case _ => throw new IllegalArgumentException("Operator not supported")

    }

    filter match {
      case Some(f) => scan.setFilter(f)
      case None =>
        scan.setBatch(selectStmt.projections.size)
        scan
    }

  }

  def getColumns(selectStmt: SelectStmt): Seq[(Array[Byte], Array[Byte])] = {

    val fields = selectStmt.projections.map {
      case ExprProj(expr, _, _) => expr match {
        case FieldIdent(_, name, _, _) => name
      }
      case StarProj(_) => "*"
    }

    fields.map(getcolumnMeta(_))

  }


  def getcolumnMeta(field: String): (Array[Byte], Array[Byte]) = {
    val schemaField = field.split("\\.")
    if (schemaField.length == 2)
      (Bytes.toBytes(schemaField.head), Bytes.toBytes(schemaField.last))
    else
      throw new IllegalArgumentException("Field must be in the format <Column_family>.<Column_name>")
  }

  def getTable(selectStmt: SelectStmt): (String, String) = {

    val parsedTable = selectStmt.relations match {
      case Some(relns) => relns.map {
        case TableRelationAST(name, _, _) => name
        case SubqueryRelationAST(_, _, _) => throw new IllegalArgumentException("Sub query not supported.")
      }
      case None =>
        throw new IllegalArgumentException("Unable to extract table information")
    }

    if (parsedTable.size > 1)
      throw new IllegalArgumentException("Multiple table definition found. Currently single table definition is supported")


    val schema = parsedTable.head.split("\\.")
    if (schema.length == 1)
      ("default", schema.head)
    else
      (schema.head, schema.last)

  }

  private def parseProjections(projs: Seq[SqlProj], scan: Scan): Scan = {

    val fields = projs.map {
      case ExprProj(expr, _, _) => expr match {
        case FieldIdent(_, name, _, _) => name
      }
      case StarProj(_) => "*"
    }

    if (fields.size == 1 && fields.head.equals("*")) {
      scan.setBatch(maxBatchSize)

    } else if (fields.size > 1 && fields.contains("*")) {
      throw new IllegalArgumentException("Field must not contain '*' as the only value")
    } else {
      if (fields.forall(_.contains("."))) {
        val extractedFields = fields.map { f =>
          val f1 = f.split("\\.")(0)
          val f2 = f.split("\\.")(1)
          (f1, f2)
        }

        extractedFields.foreach {
          case (columnfamily, columnName) => scan.addColumn(Bytes.toBytes(columnfamily), Bytes.toBytes(columnName))
        }



      } else {
        throw new IllegalArgumentException("Field must be in the format <Column_family>.<Column_name>")
      }

    }

    scan
  }

}
