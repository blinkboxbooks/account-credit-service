name := "account-credit-service-v2"

lazy val buildSettings = Seq(
  organization := "com.blinkbox.books.platform",
  version := scala.io.Source.fromFile("VERSION").mkString.trim,
  scalaVersion := "2.11.4",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")
)

lazy val root = (project in file("public")).
  settings(rpmPrepSettings: _*).
  settings(buildSettings: _*).
  settings(publish := {})
