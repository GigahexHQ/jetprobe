import sbt.Keys.{version, _}
import sbt._
import com.typesafe.sbt.packager.Keys._


/**
  * @author Shad.
  */
object BuildSettings {

  lazy val basicSettings = Seq(
    organization := "com.jetprobe",
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    version := "0.2.0-SNAPSHOT",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.12.2", "2.11.8"),
    publishMavenStyle := true,
    scriptClasspath := Seq("*"),
    maintainer := "Shad Amez <shad.amezng@gmail.com>",
    packageSummary := "Jetprobe",
    homepage := Some(url("https://jetprobe.com")),
    scmInfo := Some(ScmInfo(url("https://github.com/jetprobe/jetprobe"),
      "git@github.com:jetprobe/jetprobe.git")),
    developers := List(Developer("amezng",
      "Shad Amez",
      "shad.amezng@gmail.com",
      url("https://github.com/amezng"))),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    pomIncludeRepository := (_ => false)
  )

}
