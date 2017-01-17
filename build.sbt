lazy val root = (project in file(".")).
  settings(
    organization := "ch.becompany",
    name := "akka-social-stream",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Settings.dependencies,

    // github pages
    ghpages.settings,
    git.remoteRepo := "git@github.com:becompany/akka-social-stream.git"
  ).
  enablePlugins(SiteScaladocPlugin)