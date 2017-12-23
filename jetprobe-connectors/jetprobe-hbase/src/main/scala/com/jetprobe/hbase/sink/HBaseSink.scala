package com.jetprobe.hbase.sink

import com.jetprobe.core.sink.DataSink
import com.jetprobe.hbase.validation.HBaseConditionalQueries
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.ConnectionFactory
/**
  * @author Shad.
  */
case class HBaseSink(conf : Map[String,String]) extends DataSink with HBaseConditionalQueries{

  def getConnection : Connection = {

    val hbaseConf = HBaseConfiguration.create()

    conf.foreach {
      case (k,v) => hbaseConf.set(k,v)
    }

    ConnectionFactory.createConnection(hbaseConf)

  }

}


