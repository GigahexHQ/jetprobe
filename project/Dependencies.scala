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
  //private val rabbitMQ = "com.rabbitmq" % "amqp-client" % "3.6.5"

  private val json4s = "org.json4s" %% "json4s-native" % "3.5.2"
  private val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.5.2"
  private val combinator = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"
  private val jsonPath = "com.jayway.jsonpath" % "json-path" % "2.4.0"
  private val circeCore = "io.circe" %% "circe-core" % "0.8.0"
  private val circeGeneric = "io.circe" %% "circe-generic" % circeCore.revision
  private val circeParser = "io.circe" %% "circe-parser" % circeCore.revision
  private val sourceCode = "com.lihaoyi" %% "sourcecode" % "0.1.4"


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
  private val jongo = "org.jongo" % "jongo" % "1.3.0"
  private val jMongo = "org.mongodb" % "mongo-java-driver" % "3.4.3"
  private val mongoScala = "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"

  val mongoDeps = Seq(
    jongo,
    jMongo,
    mongoScala,
    slf4jLog4j
  )


  /** *****************************
    * Test dependencies
    * ****************************/
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  private val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"
  private val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaActor.revision % "test"
  private val mockitoCore = "org.mockito" % "mockito-core" % "2.3.11" % "test"

  val coreDependencies = Seq(
    jMongo,
    jongo,
    sourceCode,
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
    scalaTest,
    combinator,
    ahcNettyUtils,
    jsonPath,
    circeCore,
    circeParser,
    circeGeneric
  )

  val testDependencies = Seq(scalaTest, scalaCheck, akkaTestKit, mockitoCore)
}
