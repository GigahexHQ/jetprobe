package com.jetprobe.core.fs

import java.io._
import java.nio.channels.FileChannel

import com.jetprobe.core.storage.{Storage, StorageQuery}
import com.jetprobe.core.validations.ValidationRule


/**
  * @author Shad.
  */
class FileStorage(path: String) extends Storage {

  /**
    * Move the file to the destination
    * @param destination
    */
  def moveTo(destination: String): Unit = {
    new File(path).renameTo(new File(destination))
  }

  /**
    * Write the data to the file
    * @param data
    */
  def write(data: Iterator[String]): Unit = {
    val f = new File(path)

    if (!f.exists()) {
      f.createNewFile()
    }
    val writer = new BufferedWriter(new FileWriter(f))
    data.foreach { s =>
      writer.write(s)
    }
    writer.flush()
    writer.close()
  }

  /**
    * Copy the file to the destination
    * @param destination
    */
  def copyTo(destination: String): Unit = {
    var sourceChannel: FileChannel = null
    var destChannel: FileChannel = null
    try {
      sourceChannel = new FileInputStream(new File(path)).getChannel
      destChannel = new FileOutputStream(new File(destination)).getChannel
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size)
    } finally {
      sourceChannel.close()
      destChannel.close()
    }
  }

  /**
    * Delete the file
    * @return true if success else false
    */
  def rm : Boolean = new File(path).delete()


}
