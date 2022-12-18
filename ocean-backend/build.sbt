import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "ocean-backend",
    idePackagePrefix := Some("org.abteilung6.ocean"),
    libraryDependencies ++= coreDependencies ++ akkaDependencies ++ logDependencies ++ testDependencies,
    fullRunTask(runMigrate, Compile, "org.abteilung6.ocean.db.DBMigrateCommand"),
    fork / runMigrate := true,
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)
  )

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

addCommandAlias("format", "scalafmt; Test / scalafmt")
addCommandAlias("formatCheck", "scalafmtCheck; Test / scalafmtCheck")
addCommandAlias("cov", "clean; coverage; test; coverageReport;")
addCommandAlias("run-db-migrate", "runMigrate")
