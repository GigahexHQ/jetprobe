package com.jetprobe.hbase.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser, SQLParser}
import com.jetprobe.core.validations.{RuleValidator, ValidationResult, ValidationRule}
import com.jetprobe.hbase.sink.HBaseSink
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{Connection, ResultScanner, Scan}
import org.apache.hadoop.hbase.util.Bytes
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/**
  * @author Shad.
  */


class HBaseTable(namespace: Expr, tableName: Expr)

class HBaseSQL[T](val query: Expr, val decoder: String => T)

trait HBaseConditionalQueries {

  def table(namespace: String, tableName: String): HBaseTable = new HBaseTable(Expr(namespace), Expr(tableName))

  def select[T](query: String)(implicit decoder: String => T): HBaseSQL[T] = new HBaseSQL[T](Expr(query), decoder)


}


case class HBaseSQLRule[T](validationFn: Seq[T] => Any, query: Expr, decoder: String => T, hbaseConn: Connection = null)
  extends ValidationRule[HBaseSink] with RuleValidator {

  import org.json4s.native.Serialization

  implicit val formats = org.json4s.DefaultFormats

  override def validate(config: Map[String, Any]): ValidationResult = {

    val results = fetch(config)
    validateResponse[Seq[T]](results, validationFn)

  }


  override def name: String = s"HBase Query validation "

  private[this] def fetch(config: Map[String, Any]): Future[Either[Exception, Seq[T]]] = {

    val sQLParser = new SQLParser

    val parsedSQL = ExpressionParser.parse(query.value, config) flatMap { sql =>
      sQLParser.parse(sql)
    }

    Future {
      parsedSQL match {
        case Some(sqlStmt) =>

          try {
            val (namespace,tb) = HBaseQueryBuilder.getTable(sqlStmt)
            val tableName = TableName.valueOf(namespace,tb)


            println(s"looking for the table : ${tableName.getNameAsString}")
            val admin = hbaseConn.getAdmin

            if (!admin.isTableAvailable(tableName)) {
              throw new Exception(s"Table : ${tableName.getNameAsString} is not available")
            }
            val table = hbaseConn.getTable(tableName)
            val scan = new Scan()
            scan.setLoadColumnFamiliesOnDemand(true)
            HBaseQueryBuilder.parseQuery(scan,sqlStmt)
            val resultScanner = table.getScanner(scan)
            val result = getColumnValues(resultScanner, HBaseQueryBuilder.getColumns(sqlStmt)).map(decoder(_))
            Right(result)
          } catch {
            case e: Exception =>
              e.printStackTrace()
              Left(e)
          }


        case None => Left(new Exception("Unable to parse the sql statement"))

      }
    }

  }

  private def getColumnValues(rs: ResultScanner, cfs: Seq[(Array[Byte], Array[Byte])]): Seq[String] = {

    var c = 0

    val columnVals: ArrayBuffer[String] = ArrayBuffer.empty
    rs.asScala.foreach { result =>
      val rowVal = cfs.map {
        case (cf, cq) =>

          val cellValue = result.getValue(cf, cq)
          Bytes.toString(cq) -> Bytes.toString(cellValue)
      }.toMap
      c = c + 1
      println(s"Row count : $c")
      columnVals += (Serialization.write(rowVal))
    }
    rs.close()
    columnVals

  }

}
