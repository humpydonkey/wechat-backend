name := "wechat-backend"

version := "1.0"

lazy val `wechat-backend` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  filters,
  javaWs,
  "com.fasterxml.jackson.core"    % "jackson-databind"          % "2.5.3",
  "com.google.guava"              % "guava"                     % "19.0",
  "com.google.inject"             % "guice"                     % "4.0",
  "org.mongodb"                   % "mongodb-driver"            % "3.2.1",
  "org.mongojack"                 % "mongojack"                 % "2.6.1"
    // Upgrading jackson-databind causes various compile and test failures, and appears unnecessary:
    exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.projectlombok"             % "lombok"                    % "1.16.8",// https://mvnrepository.com/artifact/com.github.binarywang/weixin-java-common
  "com.github.binarywang"         % "weixin-java-mp"            % "2.2.3",
  "com.github.binarywang"         % "weixin-java-common"        % "2.2.3"
)

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
