import sbt._

/**
  * @author Shad.
  */
object Dependencies {

  //Common dependencies
  private val ahc = "org.asynchttpclient" % "async-http-client" % "2.1.0-alpha19"
  private val ahcNettyUtils = "org.asynchttpclient" % "async-http-client-netty-utils" % ahc.revision
  private val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.2"
  private val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaActor.revision
  private val slf4jLog4j = "org.slf4j" % "slf4j-log4j12" % "1.7.23"
  private val scopt = "com.github.scopt" %% "scopt" % "3.5.0"
  private val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  private val csvReader = "com.github.tototoshi" %% "scala-csv" % "1.3.4"

  private val json4s = "org.json4s" %% "json4s-native" % "3.5.2"
  private val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.5.2"
  private val combinator = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"
  private val jsonPath = "com.jayway.jsonpath" % "json-path" % "2.4.0"
  private val circeCore = "io.circe" %% "circe-core" % "0.8.0"
  private val circeGeneric = "io.circe" %% "circe-generic" % circeCore.revision
  private val circeParser = "io.circe" %% "circe-parser" % circeCore.revision
  private val circeExtras = "io.circe" %% "circe-generic-extras" % circeCore.revision
  private val circeYaml = "io.circe" %% "circe-yaml" % "0.6.1"
  private val sourceCode = "com.lihaoyi" %% "sourcecode" % "0.1.4"
  private val snakeYaml = "org.yaml" % "snakeyaml" % "1.19"
  private val scalatags = "com.lihaoyi" %% "scalatags" % "0.6.7"
  private val sshLib = "com.hierynomus" % "sshj" % "0.23.0"
  private val fastParse = "com.lihaoyi" %% "fastparse" % "1.0.0"

  private val junit = "junit" % "junit" % "4.12"



  /***************************************
  *  RabbitMQ connector dependencies
   ***************************************/
  private val rabbitClient = "com.rabbitmq" % "amqp-client" % "3.6.5"
  private val rabbitAdminClient = "com.rabbitmq" % "http-client" % "1.3.0.RELEASE"

  val rabbitDeps = Seq(
    rabbitClient,
    rabbitAdminClient,
    slf4jLog4j
  )

  /***************************************
    *  Mongo connector dependencies
    ***************************************/
  private val jMongo = "org.mongodb" % "mongo-java-driver" % "3.4.3"
  private val mongoScala = "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"


  val mongoDeps = Seq(
    jMongo,
    mongoScala,
    slf4jLog4j
  )

  /************************************
    * Hadoop connector dependencies
    **********************************/
  private val hadoopClient = "org.apache.hadoop" % "hadoop-client" % "2.7.4" excludeAll(
    ExclusionRule("io.netty", "netty-all"),
    ExclusionRule("io.netty", "netty")
  )
  private val hadoopHDFS =  "org.apache.hadoop" % "hadoop-hdfs" % hadoopClient.revision excludeAll(
    ExclusionRule("io.netty", "netty-all"),
    ExclusionRule("io.netty", "netty")
  )
  private val hadoopCommon = "org.apache.hadoop" % "hadoop-common" % hadoopClient.revision exclude("io.netty", "netty-all")

  val hadoopDeps = Seq(hadoopClient,hadoopHDFS)

  /************************************
    * HBase Dependencies
    **********************************/
  private val hbaseClient = "org.apache.hbase" % "hbase-client" % "1.2.0" exclude("io.netty", "netty-all")
  private val hbaseCommon = "org.apache.hbase" % "hbase-common" % hbaseClient.revision

  val hbaseDeps = Seq(hbaseClient,hbaseCommon,hadoopCommon)

  /************************************
    * Consul connector dependencies
    ***********************************/
  private val consulClient = "com.ecwid.consul" % "consul-api" % "1.3.0"

  val consulDeps = Seq(
    consulClient
  )


  /** *****************************
    * Test dependencies
    * ****************************/
  private val scalactic = "org.scalactic" %% "scalactic" % "3.0.3"
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3" % "test"
  private val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"
  private val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaActor.revision % "test"
  private val mockitoCore = "org.mockito" % "mockito-core" % "2.3.11" % "test"

  /*****************************
    *  Reporting dependencies
    ****************************/
  private val extentReport = "com.aventstack" % "extentreports" % "3.1.2"


  val coreDependencies = Seq(
    sshLib,
    fastParse,
    jMongo,
    combinator,
    snakeYaml,
    sourceCode,
    scalatags,
    ahc,
    akkaActor,
    akkaSlf4j,
    scopt,
    scalaLogging,
    csvReader,
    slf4jLog4j,
    mongoScala,
    json4s,
    json4sJackson,
    ahcNettyUtils,
    jsonPath,
    circeCore,
    circeYaml,
    circeParser,
    circeExtras,
    circeGeneric,
    junit,
    extentReport
  )



  //val reportDependencies = Seq(extentReport)

  val testDependencies = Seq(scalactic,scalaTest, scalaCheck, akkaTestKit, mockitoCore)
}
