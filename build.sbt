lazy val root = (project in file(".")).
  settings(
    organization := "ch.becompany",
    name := "scala-social-feed",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Settings.dependencies
  )
