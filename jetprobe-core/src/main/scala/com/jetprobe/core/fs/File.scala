package com.jetprobe.core.fs

/**
  * @author Shad.
  */

/**
  * File Config Builder
  */
object File {

  def at(path : String) : FilePath = new FilePath(path)

}

object Dir {
  def at(path : String) : DirPath = new DirPath(path)
}
