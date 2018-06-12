package com.jetprobe.hbase.validation

import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult}
import com.jetprobe.hbase.storage.HBaseStorage
import org.apache.hadoop.hbase.TableName

/**
  * @author Shad.
  */
case class TableValidator( hBaseStorage: HBaseStorage, name : String, namespace : String = "default") extends ValidationExecutor[HBaseStorage]{

  val defaultError = throw new Exception(s"Unable to fetch the table info for : ${name}")

  def exists : ValidationResult = assertThat(true, hBaseStorage){ hbase =>
   val result = hbase.usingAdmin(admin => admin.isTableAvailable(TableName.valueOf(namespace.getBytes,name.getBytes)))
    result.getOrElse(defaultError)
  }

  def hasColumnFamilies(families : Array[String]) = assertThat(families,hBaseStorage){hbase =>

    val result = hbase
      .usingAdmin(admin => admin.getTableDescriptor(TableName.valueOf(namespace.getBytes,name.getBytes)).getColumnFamilies.map(f => new String(f.getName)))
    result.getOrElse(defaultError)
  }


  def notExists : ValidationResult = assertThat(false, hBaseStorage){ hbase =>
    val result = hbase.usingAdmin(admin => admin.isTableAvailable(TableName.valueOf(namespace.getBytes,name.getBytes)))
    result.getOrElse(throw new Exception(s"Unable to fetch the table info for : ${name}"))
  }






}
