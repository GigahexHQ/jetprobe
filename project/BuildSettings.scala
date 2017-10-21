import sbt.Keys.{version, _}
import sbt._

/**
  * @author Shad.
  */
object BuildSettings {

  lazy val basicSettings = Seq(
    organization := "com.jetprobe",
    version := "1.0",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.12.2", "2.11.8")
  )

}
