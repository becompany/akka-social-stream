import sbt._

object Settings {

  object Versions {
    val twitter4j = "4.0.5"
    val akka = "2.4.12"
    val scalaTest = "3.0.0"
    val akkaHttp = "10.0.0"
    val kOAuth = "1.1.0"
    val scalaCache = "0.9.3"
  }

  object Dependencies {
    val twitter4jAsync = "org.twitter4j" % "twitter4j-async" % Versions.twitter4j
    val twitter4jStream = "org.twitter4j" % "twitter4j-stream" % Versions.twitter4j
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka
    val akkaHttp = "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Versions.akkaHttp
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
    val kOAuth = "com.hunorkovacs" %% "koauth" % Versions.kOAuth
    val scalaCacheCaffeine = "com.github.cb372" %% "scalacache-caffeine" % Versions.scalaCache
  }

  val dependencies: Seq[ModuleID] = {
    import Dependencies._
    Seq(
      twitter4jAsync,
      twitter4jStream,
      akkaStream,
      akkaStreamTestkit,
      akkaHttp,
      akkaHttpSprayJson,
      scalaTest,
      kOAuth,
      scalaCacheCaffeine
    )
  }

}