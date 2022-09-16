import sbt._

object Dependencies {

  object Version {
    val AkkaVersion = "2.6.19"
    val AkkaHttpVersion = "10.2.9"
    val macwireVersion = "2.5.8"
    val LogbackVersion = "1.2.11"
    val scalaTestVersion = "3.2.13"
    val scalaMockitoVersion = "3.2.12.0"
  }

  val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % Version.AkkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.AkkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.AkkaHttpVersion
  val macwire = "com.softwaremill.macwire" %% "macros" % Version.macwireVersion % Provided
  val logback = "ch.qos.logback" % "logback-classic" % Version.LogbackVersion
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTestVersion
  val mockito = "org.scalatestplus" %% "mockito-4-5" % Version.scalaMockitoVersion

  lazy val akkaDependencies: Seq[ModuleID] = Seq(akkaActor, akkaStream, akkaHttp)
  lazy val coreDependencies: Seq[ModuleID] = Seq(macwire)
  lazy val logDependencies: Seq[ModuleID] = Seq(logback)
  lazy val testDependencies: Seq[ModuleID] = Seq(scalaTest, mockito)
}
