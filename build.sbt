import BuildSettings.basicSettings
import Dependencies._


lazy val root = Project("jetprobe", file("."))
  .dependsOn(Seq(commons, core,rabbitConnector,mongoConnector).map(_ % "compile->compile;test->test"): _*)
  .settings(basicSettings: _*)

def jetProbeModule(id: String) = Project(id, base = file(id))

def jetProbeConnector(id: String) = Project(id, base = file("jetprobe-connectors/" + id))

packMain := Map("jetprobe" -> "com.jetprobe.core.runner.TestRunner")

lazy val commons = jetProbeModule("jetprobe-common")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)

lazy val core = jetProbeModule("jetprobe-core")
  .dependsOn(commons % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)
.enablePlugins(PackPlugin)

lazy val rabbitConnector = jetProbeConnector("jetprobe-rabbitmq")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= rabbitDeps)

lazy val mongoConnector = jetProbeConnector("jetprobe-mongo")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= mongoDeps)

lazy val samples = jetProbeModule("jetprobe-sample")
  .dependsOn(core % "compile->compile")
  .dependsOn(mongoConnector % "compile->compile")
  .dependsOn(rabbitConnector % "compile->compile")
  .settings(basicSettings: _*)


scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")


