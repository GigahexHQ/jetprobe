import BuildSettings.basicSettings
import Dependencies._

mainClass in Compile := Some("com.jetprobe.core.runner.TestRunner")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
sonatypeProfileName := "com.jetprobe"

def jetProbeModule(id: String) = Project(id, base = file(id))

def jetProbeConnector(id: String) = Project(id, base = file("jetprobe-connectors/" + id))

packMain := Map("jetprobe" -> "com.jetprobe.core.runner.TestRunner")

packResourceDir += (baseDirectory.value / "web/static" -> "static")

lazy val core = jetProbeModule("jetprobe-core")
  //.dependsOn(commons % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)
  .enablePlugins(PackPlugin)

lazy val consulConnector = jetProbeConnector("jetprobe-consul")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= consulDeps ++ testDependencies)


lazy val rabbitConnector = jetProbeConnector("jetprobe-rabbitmq")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= rabbitDeps ++ testDependencies)

lazy val mongoConnector = jetProbeConnector("jetprobe-mongo")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= mongoDeps ++ testDependencies)


//This subproject is for quick testing..
lazy val samples = jetProbeModule("jetprobe-sample")
  .dependsOn(core % "compile->compile")
  .dependsOn(mongoConnector % "compile->compile")
  .dependsOn(rabbitConnector % "compile->compile")
  .dependsOn(consulConnector % "compile->compile")
  .settings(basicSettings: _*)



scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")


