import AssemblyKeys._

name := "account-credit-service-v2"

scalaVersion in ThisBuild := "2.11.4"

lazy val buildSettings = Seq(
  organization := "com.blinkbox.books.platform",
  version := scala.io.Source.fromFile("VERSION").mkString.trim,
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")
)

lazy val artifactSettings = addArtifact(artifact in (Compile, assembly), assembly).settings

lazy val root = (project in file(".")).
  dependsOn(public).aggregate(public).
  settings(publish := {})

lazy val public = (project in file("public")).
  settings(aggregate in publish := false).
  settings(buildSettings: _*).
  settings(rpmPrepSettings: _*).
  settings(artifactSettings: _*).
  settings(publish := {})
