package com.jetprobe.core.fs

import java.io.File

import com.jetprobe.core.validations.Passed
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Shad.
  */
class FileStorageSpec extends FlatSpec with Matchers {

  behavior of "File Storage"

  it should "parse the file path" in {
    val fp = getClass.getResource("/fs/data.in").getPath
    val config = Map("file.path" -> fp)
    val filePath = File.at("${file.path}")

    val fileStorage = filePath.getStorage(config)
    var storagePath = fileStorage.underLyingFile.getAbsoluteFile.getPath
    if (System.getenv("os").toLowerCase().contains("windows")) {
      storagePath = "/" + storagePath.replaceAll("\\\\", "/")
    }

    fp shouldEqual storagePath
  }

  it should "move the file" in {
    val fp = getClass.getResource("/fs/movable.in")
    var incPath = "/moved.in"
    if (System.getenv("os").toLowerCase().contains("windows")) {
      incPath = """\moved.in"""
    }
    val newPath = new File(fp.getPath).getParent + incPath
    val f = File.at(fp.getPath).getStorage(Map.empty)
    val moved = f.moveTo(newPath)

    if(moved){
      assert(!new File(fp.getPath).exists())
    } else
      assert(f.underLyingFile.exists())

  }

  it should "validate for line count" in {
    val fp = getClass.getResource("/fs/data.in").getPath
    val f = File.at(fp).getStorage(Map.empty)

    f.hasTotalLines(5).status shouldEqual(Passed)
    f.hasLines(_.equals("5")).status shouldEqual(Passed)
    f.hasLines(1)(_.equals("2")).status shouldEqual(Passed)
  }

}
