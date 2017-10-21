package com.jetprobe.mongo.models

import com.jetprobe.core.sink.DataSource

/**
  * @author Shad.
  */
case class CollectionStats(ns: String,
                           count: Int,
                           size : Int,
                           avgObjSize : Int,
                           storageSize : Int,
                           capped : Boolean,
                           nindexes : Int,
                           totalIndexSize : Int) extends DataSource

case class DatabaseList(
                         databases : Array[DBInfo],
                         totalSize : Double,
                         ok : Double
                       ) extends DataSource

case class DBInfo(name : String,
                  sizeOnDisk : Double,
                  empty : Boolean)

case class DBStats(db : String,
                   collections : Int,
                   views : Int,
                   objects : Int,
                   avgObjSize : Double,
                   dataSize : Double,
                   storageSize : Double,
                   numExtents : Int,
                   indexes : Int,
                   indexSize : Int,
                   ok : Double) extends DataSource


case class ServerStats(version: String,
                       connections: ConnectionInfo,
                       opcounters: OpCounters,
                       storageEngine: Option[StorageEngine],
                       mem : Memory)
                    extends DataSource

//network: NetworkStats)

case class ConnectionInfo(current: Int, available: Int, totalCreated: Int)

case class OpCounters(insert: Long, query: Long, update: Long, delete: Long, getmore: Long, command: Long)

case class Memory(bits : Int, resident : Int, virtual : Int, supported : Boolean, mapped : Option[Int], mappedWithJournal : Option[Int])

case class StorageEngine(name : String, supportsCommittedReads : Boolean, readOnly : Boolean, persistent : Boolean)

case class SourceBsonDocuments[T](count: Int,
                                  documents: Iterator[T]) extends DataSource


