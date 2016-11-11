import sbt._

object Settings {

  object Versions {
    val twitter4j = "4.0.5"
    val akka = "2.4.12"
    val scalaTest = "3.0.0"
  }

  object Dependencies {
    val twitter4jAsync = "org.twitter4j" % "twitter4j-async" % Versions.twitter4j
    val twitter4jStream = "org.twitter4j" % "twitter4j-stream" % Versions.twitter4j
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }

  val dependencies: Seq[ModuleID] = {
    import Dependencies._
    Seq(
      twitter4jAsync,
      twitter4jStream,
      akkaStream,
      akkaStreamTestkit,
      scalaTest
    )
  }

}