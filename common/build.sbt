name := "account-credit-service-v2-common"

libraryDependencies ++= {
  Seq(
    "com.blinkbox.books"          %% "common-spray"             % "0.24.0",
    "com.blinkbox.books"          %% "common-spray-auth"        % "0.7.6",
    "com.blinkbox.books"          %% "common-config"            % "2.3.1",
    "com.blinkbox.books"          %% "common-slick"             % "0.3.4",
    "io.spray"                    %% "spray-testkit"            % "1.3.2" % Test,
    "com.blinkbox.books"          %% "common-scala-test"        % "0.3.0" % Test,
    "io.spray"                    %% "spray-testkit"            % "1.3.2" % "test",
    "com.github.tototoshi"        %% "slick-joda-mapper"        % "1.2.0",
    "mysql"                        % "mysql-connector-java"     % "5.1.34",
    "org.apache.commons"           % "commons-dbcp2"            % "2.0.1",
    "joda-time"                    % "joda-time"                % "2.7",
    "org.joda"                     % "joda-money"               % "0.10.0",
    "com.h2database"               % "h2"                       % "1.4.185" % Test,
    "com.google.guava"             % "guava"                    % "18.0" % Test
  )
}

parallelExecution in Test := false
