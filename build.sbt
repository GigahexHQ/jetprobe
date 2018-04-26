import BuildSettings.basicSettings
import Dependencies._

mainClass in Compile := Some("com.jetprobe.core.runner.TestRunner")

resolvers += Resolver.typesafeIvyRepo("releases")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
sonatypeProfileName := "com.jetprobe"

lazy val root = Project("jetprobe", file("."))
  .dependsOn(Seq(core,rabbitConnector,mongoConnector,consulConnector,hbaseConnector,hadoopConnector).map(_ % "compile->compile;test->test"): _*)
  .settings(basicSettings: _*)
  .enablePlugins(JavaAppPackaging)
    .aggregate(core,hadoopConnector,hbaseConnector)

def jetProbeModule(id: String) = Project(id, base = file(id))

def jetProbeConnector(id: String) = Project(id, base = file("jetprobe-connectors/" + id))

packMain := Map("jetprobe" -> "com.jetprobe.core.runner.JetprobeCLI")

packResourceDir += (baseDirectory.value / "web/static" -> "static")

lazy val commons = jetProbeModule("jetprobe-common")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)

lazy val core = jetProbeModule("jetprobe-core")
  //.dependsOn(commons % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies ++ testDependencies)
  .enablePlugins(PackPlugin)


lazy val hadoopConnector = jetProbeConnector("jetprobe-hadoop")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= hadoopDeps ++ testDependencies)



lazy val hbaseConnector = jetProbeConnector("jetprobe-hbase")
  .dependsOn(core % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= hbaseDeps ++ testDependencies)


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
  .dependsOn(hadoopConnector % "compile->compile")
  .dependsOn(hbaseConnector % "compile->compile")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)


scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")


