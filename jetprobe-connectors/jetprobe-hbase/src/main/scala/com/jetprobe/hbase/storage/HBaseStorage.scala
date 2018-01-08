package com.jetprobe.hbase.storage

import com.jetprobe.core.storage.{DataSource, Storage}
import com.jetprobe.core.structure.Config
import com.jetprobe.hbase.validation.HBaseConditionalQueries
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory, Put}

/**
  * @author Shad.
  */
class HBaseStorage private[jetprobe](conf: Map[String, String], table: Option[TableName] = None)
  extends Storage
    with HBaseConditionalQueries {

  private[hbase] def getConnection: Connection = {

    val hbaseConf = HBaseConfiguration.create()

    conf.foreach {
      case (k, v) => hbaseConf.set(k, v)
    }

    ConnectionFactory.createConnection(hbaseConf)

  }

  private[this] def getAdmin: Admin = getConnection.getAdmin

  def createTable(name: String, namespace: String = "default", columnFamilies: Seq[String]): Unit = {

    usingAdmin { admin =>
      val tableName = TableName.valueOf(namespace, name)
      if (!admin.isTableAvailable(tableName)) {
        val descriptor = new HTableDescriptor(TableName.valueOf(namespace, name))
        columnFamilies.foreach(f => descriptor.addFamily(new HColumnDescriptor(f)))
        admin.createTable(descriptor)
      }

    }

  }

  def disable(table : String) : Unit = {
    usingAdmin{ admin =>
      admin.disableTable(TableName.valueOf(table))
    }
  }

  def describe(table : String) : Option[HTableDescriptor] = {
    usingAdmin[HTableDescriptor] {admin =>
      admin.getTableDescriptor(TableName.valueOf(table))
    }
  }

  def createNamespace(ns: String): Unit = {
    usingAdmin { admin =>
      if (admin.listNamespaceDescriptors().count(_.getName.equals(ns)) == 0) {
        val nd = NamespaceDescriptor.create(ns).build()
        admin.createNamespace(nd)
      }

    }
  }

  def listTables() : Option[Array[String]] = {
    usingAdmin[Array[String]] { admin =>
      admin.listTableNames().map(_.getNameAsString)
    }
  }

  def listTables(regex : String) : Option[Array[String]] = {
    usingAdmin[Array[String]] { admin =>
      admin.listTableNames(regex).map(_.getNameAsString)
    }
  }

  private[this] def usingAdmin[T](fn: Admin => T): Option[T] = {
    val admin = getAdmin
    try {
      Some(fn(admin))
    } catch {
      case ex: Exception =>
        logger.error(ex.getMessage)
        None
    } finally admin.close()

  }

  def dropTable(name: String, namespace: String = "default"): Unit = {
    usingAdmin { admin =>
      val table = TableName.valueOf(namespace, name)
      if (admin.isTableAvailable(table)) {
        admin.disableTable(table)
        admin.deleteTable(table)
      } else {
        throw new IllegalArgumentException(s"Table ${namespace + "." + name} not found")
      }
    }


  }

  def status: HBaseClusterStatus = {

    val admin = getAdmin
    val status = admin.getClusterStatus
    new HBaseClusterStatus(status.getVersion.toString, status.getDeadServers, status.getAverageLoad, status.getClusterId)
  }

}

class HBaseConfig(conf: Map[String, String]) extends Config[HBaseStorage] {

  override private[jetprobe] def getStorage: HBaseStorage = new HBaseStorage(conf)
}

class HBaseClusterStatus(val version: String, val deadServers: Int, val averageLoad: Double, val clusterId: String) extends DataSource
