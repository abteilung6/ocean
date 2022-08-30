import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "ocean-backend",
    idePackagePrefix := Some("org.abteilung6.ocean"),
    libraryDependencies ++= akkaDependencies ++ logDependencies
  )
