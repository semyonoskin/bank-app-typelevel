name := "typelevel-pet"

version := "0.1"

scalaVersion := "2.13.3"

val http4sVersion = "1.0.0-M5+109-c8e90397-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "2.2.0" withSources() withJavadoc(),
  "org.tpolecat" %% "doobie-core" % "0.9.0",

  // And add any of these as needed
  "org.tpolecat" %% "doobie-hikari" % "0.9.0", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "0.9.0", // Postgres driver 42.2.12 + type mappings.
  "org.tpolecat" %% "doobie-specs2" % "0.9.0" % "test", // Specs2 support for typechecking statements.
  "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test",// ScalaTest support for typechecking statements.
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.13.0",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.13.0",
  "io.chrisdavenport" %% "log4cats-slf4j"   % "1.1.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"

)

resolvers += Resolver.sonatypeRepo("snapshots")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds"
)
