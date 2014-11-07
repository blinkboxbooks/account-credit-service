name := "account-credit-service-v2-common"

libraryDependencies ++= {
  Seq(
    "com.blinkbox.books" %% "common-spray" % "0.17.5",
    "com.blinkbox.books" %% "common-config" % "1.4.1",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "io.spray" %% "spray-testkit" % "1.3.2" % "test"
  )
}
