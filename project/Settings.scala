import sbt._

object Settings {

  object Versions {
    val akka = "2.4.17"
    val akkaActor = "2.4.17-BECOMPANY"
    val scalaTest = "3.0.0"
    val akkaHttp = "10.0.0"
    val kOAuth = "1.1.0"
    val scalaCache = "0.9.3"
    val scalaLogging = "3.5.0"
    val scalaTags = "0.6.1"
    val logback = "1.1.9"
    val twitterText = "1.6.1"
  }

  object Dependencies {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akkaActor
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka
    val akkaHttp = "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Versions.akkaHttp
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
    val kOAuth = "com.hunorkovacs" %% "koauth" % Versions.kOAuth excludeAll(
      ExclusionRule(organization = "org.specs2"))
    val scalaCacheCaffeine = "com.github.cb372" %% "scalacache-caffeine" % Versions.scalaCache
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging
    val scalaTags = "com.lihaoyi" %% "scalatags" % Versions.scalaTags
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    val twitterText = "com.twitter" % "twitter-text" % Versions.twitterText
  }

  val dependencies: Seq[ModuleID] = {
    import Dependencies._
    Seq(
      akkaActor,
      akkaStream,
      akkaStreamTestkit,
      akkaHttp,
      akkaHttpSprayJson,
      scalaTest,
      kOAuth,
      scalaCacheCaffeine,
      scalaLogging,
      scalaTags,
      logback,
      twitterText
    )
  }

}
