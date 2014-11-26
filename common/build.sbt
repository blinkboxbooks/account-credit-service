name := "account-credit-service-v2-common"

libraryDependencies ++= {
  Seq(
    "com.blinkbox.books"          %% "common-spray"             % "0.17.5",
    "com.blinkbox.books"          %% "common-spray-auth"        % "0.7.4",
    "com.blinkbox.books"          %% "common-config"            % "1.4.1",
    "com.blinkbox.books"          %% "common-slick"             % "0.3.2",
    "io.spray"                    %% "spray-testkit"            % "1.3.2" % Test,
    "com.blinkbox.books"          %% "common-scala-test"        % "0.3.0" % Test,
    "com.github.tototoshi"        %% "slick-joda-mapper"        % "1.2.0",
    "mysql"                        % "mysql-connector-java"     % "5.1.34",
    "org.apache.commons"           % "commons-dbcp2"            % "2.0.1",
    "org.joda"                     % "joda-money"               % "0.9.1",
    "com.h2database"               % "h2"                       % "1.4.182" % Test
  )
}

parallelExecution in Test := false
