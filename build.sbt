lazy val root = (project in file(".")).
  settings(
    organization := "ch.becompany",
    name := "akka-social-stream",
    version := "0.1.5-SNAPSHOT",
    scalaVersion := "2.12.8",
    libraryDependencies ++= Settings.dependencies,

    // github pages
    git.remoteRepo := "git@github.com:becompany/akka-social-stream.git"
  ).
  enablePlugins(SiteScaladocPlugin, GhpagesPlugin)
