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

  def getColumns(projections: Seq[SqlProj]): Seq[HBaseColumn] = {

    SQLParser.getColumnMeta(projections).map { cm =>
      val (cf, cq) = getHBaseColumnMeta(cm.fieldName)
      HBaseColumn(cf, cq, cm.alias, cm.dataType)
    }


  }


  def getHBaseColumnMeta(field: String): (Array[Byte], Array[Byte]) = {
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

    if (parsedTable.length > 1)
      throw new IllegalArgumentException("Multiple table definition found. Currently single table definition is supported")


    val schema = parsedTable.head.split("\\.")
    if (schema.length == 1)
      ("default", schema.head)
    else
      (schema.head, schema.last)

  }

  private def parseProjections(projs: Seq[SqlProj], scan: Scan): Scan = {

    val fields = SQLParser.getColumnMeta(projs)

    if (fields.length == 1 && fields.head.fieldName.equals("*")) {
      scan.setBatch(maxBatchSize)

    } else if (fields.length > 1 && fields.head.fieldName.equals("*")) {
      throw new IllegalArgumentException("Field must not contain '*' as the only value")
    } else {

      fields.foreach { column =>
        val (cf, cv) = getHBaseColumnMeta(column.fieldName)
        scan.addColumn(cf, cv)
      }
    }

    scan
  }

}

case class HBaseColumn(columnFamily: Array[Byte], columnQualifier: Array[Byte], alias: Option[String], dataType: FieldType) {

  val column : String = Bytes.toString(columnQualifier)

  def getData(cellValue: Array[Byte]): (String, Any) = {
    val cell = dataType match {
      case IntegerType => Bytes.toInt(cellValue)
      case DoubleType => Bytes.toDouble(cellValue)
      case FloatType => Bytes.toFloat(cellValue)
      case StringType => Bytes.toString(cellValue)
    }

    alias match {
      case Some(aliasVal) => (aliasVal , cell)
      case None => (column , cell)
    }

  }

}

