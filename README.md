# Akka Social Stream

Generate a feed of social network status messages as Akka stream.

## Installation

Add the dependency to your SBT build file:

~~~ sbt
libraryDependencies += "ch.becompany" %% "akka-social-stream" % "0.1.0-SNAPSHOT"
~~~

## Usage

Declare a stream for Twitter tweets and GitHub events:

~~~ scala
val feed = new Feed(
  "twitter" -> new TwitterFeed(Some("my_screen_name")),
  "github" -> new GithubFeed("my_github_organization")
)
~~~

Stream the latest 10 and all subsequent status messages (see [`Status`]() class for details):

~~~ scala
feed.source(10).runForeach { case (network, status) =>
  println(s"Status update on $network by ${status.author.name}")
}
~~~

## API documentation

* [Latest](http://becompany.github.io/akka-social-stream/latest/api/)
