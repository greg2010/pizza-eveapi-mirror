name := "eveapi"

organization := "moe.pizza"

scalaVersion := "2.12.2"

isSnapshot := true

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

lazy val buildSources = taskKey[Unit]("Execute the XML build script")

buildSources := {
  "./build.sh" !
}

compile <<= (compile in Compile) dependsOn buildSources
test <<= (test in Test) dependsOn buildSources

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishTo := Some("Artifactory Realm" at "http://maven.red.greg2010.me/artifactory/sbt-local")

val dispatchVersion = "0.12.0"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion
resolvers += Resolver.bintrayRepo("andimiller", "maven")

val slickVersion = "3.2.0"
val HTTP4S_VERSION = "0.15.0a"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.scala-lang.modules"       %% "scala-xml"                % "1.0.6",
  "org.scala-lang.modules"       %% "scala-parser-combinators" % "1.0.6",
  "org.http4s"                   %% "http4s-blaze-client"      % HTTP4S_VERSION,
  "org.http4s"                   %% "http4s-scala-xml"         % HTTP4S_VERSION,
  "org.http4s"                   %% "http4s-circe"             % HTTP4S_VERSION,
  "net.debasishg"                %% "redisclient"              % "3.4",
  "org.json4s"                   %% "json4s-jackson"           % "3.5.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala"     % "2.8.8",
  "org.eclipse.jetty.websocket"  % "websocket-client"          % "9.3.5.v20151012",
  "joda-time"                    % "joda-time"                 % "2.8.2",
  "org.joda"                     % "joda-convert"              % "1.2",
  "org.slf4j"                    % "slf4j-simple"              % "1.7.12",
  "com.typesafe.slick"           %% "slick"                    % slickVersion,
  "com.typesafe.slick"           %% "slick-codegen"            % slickVersion,
  "mysql"                        % "mysql-connector-java"      % "5.1.37"
)

libraryDependencies ++= Seq (
  "org.scalatest"          %% "scalatest" % "3.0.3" % "test",
  "com.github.tomakehurst" % "wiremock"   % "1.33"  % "test"
)

lazy val slickGenerate = taskKey[Seq[File]]("slick code generation")

slickGenerate := {
  val url = "jdbc:mysql://localhost:3306/sde"
  val jdbcDriver = "com.mysql.jdbc.Driver"
  val slickDriver = "slick.driver.MySQLDriver"
  val targetPackageName = "moe.pizza.sdeapi"
  val outputDir = "./src/main/scala/"//((sourceManaged in Compile).value / "sde").getPath // place generated files in sbt's managed sources folder
  val fname = outputDir + s"/$targetPackageName/Tables.scala"
  println(s"\nauto-generating slick source for database schema at $url...")
  println(s"output source file file: file://$fname\n")
  (runner in Compile).value.run("slick.codegen.SourceCodeGenerator", (dependencyClasspath in Compile).value.files, Array(slickDriver, jdbcDriver, url, outputDir, targetPackageName, "sde", "sde"), streams.value.log)
  Seq(file(fname))
}

//compile <<= (compile in Compile) dependsOn slickGenerate


ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
