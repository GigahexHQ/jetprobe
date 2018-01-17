package com.jetprobe.hbase.validation

import com.jetprobe.core.parser._
import com.jetprobe.core.storage.StorageQuery
import com.jetprobe.core.validations.{RuleValidator, ValidationResult, ValidationRule}
import com.jetprobe.hbase.storage.HBaseStorage
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, Scan}
import sourcecode.{FullName, Line}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/**
  * @author Shad.
  */


class HBaseTable(namespace: Expr, tableName: Expr)

case class HBaseSQL[T](query: Expr, decoder: String => T) extends StorageQuery[HBaseStorage,T] {

  override def build(fn: Seq[T] => Any, line: Line, fullName: FullName): ValidationRule[HBaseStorage] = {
    HBaseSQLRule(fn,query,decoder,line,fullName)
  }


}

trait HBaseConditionalQueries {

  def table(namespace: String, tableName: String): HBaseTable = new HBaseTable(Expr(namespace), Expr(tableName))

  def select[T](query: String, decoder: String => T): HBaseSQL[T] = new HBaseSQL[T](Expr(query), decoder)


}



case class HBasePropertyRule[T](value : T, validationFn : T => Any,hbaseConn : Connection = null)
extends ValidationRule[HBaseStorage] with RuleValidator {

  override def name: String = s"HBase Property validation for $value"

  override def validate(config: Map[String, Any],storage: HBaseStorage): ValidationResult = {
    validateResponse(value,validationFn)
  }
}


case class HBaseSQLRule[T](validationFn: Seq[T] => Any, query: Expr, decoder: String => T, line: sourcecode.Line, fullName: sourcecode.FullName)
  extends ValidationRule[HBaseStorage] with RuleValidator with SqlExecutor {

  import org.json4s.native.Serialization

  implicit val formats = org.json4s.DefaultFormats

  override def validate(config: Map[String, Any],storage: HBaseStorage): ValidationResult = {

    val results = fetch(config,storage)
    validateResponse[Seq[T]](results, validationFn,this)

  }


  override def name: String = s"Test at ${fullName.value}:${line.value}"

  private[this] def fetch(config: Map[String, Any],storage: HBaseStorage): Future[Either[Throwable, Seq[T]]] = {

    execute(query.value,config,storage,queryHandler,decoder)

  }

  def queryHandler(sqlStmt: SelectStmt,storage : HBaseStorage) : Seq[String] = {


    val (namespace,tb) = HBaseQueryBuilder.getTable(sqlStmt)
    val tableName = TableName.valueOf(namespace,tb)
    val hbaseConn = storage.getConnection
    val admin = hbaseConn.getAdmin

    if (!admin.isTableAvailable(tableName)) {
      throw new Exception(s"Table : ${tableName.getNameAsString} is not available")
    }
    val table = hbaseConn.getTable(tableName)
    val scan = new Scan()

    scan.setLoadColumnFamiliesOnDemand(true)
    HBaseQueryBuilder.parseQuery(scan,sqlStmt)

    val resultScanner = table.getScanner(scan)
    val columnVals: ArrayBuffer[String] = ArrayBuffer.empty

    val cfs = HBaseQueryBuilder.getColumns(sqlStmt.projections)
    resultScanner.asScala.foreach { result =>
      val rowVal = cfs.map {
        case col : HBaseColumn =>

          val cellValue = result.getValue(col.columnFamily,col.columnQualifier)
          col.getData(cellValue)

      }.toMap
      columnVals += (Serialization.write(rowVal))
    }
    resultScanner.close()
    columnVals
  }


}
