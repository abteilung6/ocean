import sbt._

object Dependencies {

  object Version {
    val akkaVersion = "2.6.20"
    val akkaHttpVersion = "10.2.9"
    val macwireVersion = "2.5.8"
    val directoryApiVersion = "2.1.2"
    val slickVersion = "3.3.3"
    val postgresVersion = "42.5.0"
    val flywayVersion = "9.3.0"
    val logbackVersion = "1.2.11"
    val scalaTestVersion = "3.2.13"
    val scalaMockitoVersion = "3.2.12.0"
  }

  val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % Version.akkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttpVersion
  val macwire = "com.softwaremill.macwire" %% "macros" % Version.macwireVersion % Provided
  val directoryApi = "org.apache.directory.api" % "api-all" % Version.directoryApiVersion
  val slick = "com.typesafe.slick" %% "slick" % Version.slickVersion
  val hikaricp = "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
  val postgres = "org.postgresql" % "postgresql" % Version.postgresVersion
  val flyway = "org.flywaydb" % "flyway-core" % Version.flywayVersion
  val h2 = "com.h2database" % "h2" % "2.1.214" % Test
  val logback = "ch.qos.logback" % "logback-classic" % Version.logbackVersion
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTestVersion
  val mockito = "org.scalatestplus" %% "mockito-4-5" % Version.scalaMockitoVersion

  lazy val akkaDependencies: Seq[ModuleID] = Seq(akkaActor, akkaStream, akkaHttp)
  lazy val coreDependencies: Seq[ModuleID] = Seq(macwire, directoryApi, slick, hikaricp, postgres, flyway, h2)
  lazy val logDependencies: Seq[ModuleID] = Seq(logback)
  lazy val testDependencies: Seq[ModuleID] = Seq(scalaTest, mockito)
}
