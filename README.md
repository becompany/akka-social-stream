# Akka Social Stream

Generate a feed of social network status messages as Akka stream.

## Installation

Add the dependency to your SBT build file:

~~~ sbt
libraryDependencies += "ch.becompany" %% "akka-social-stream" % "0.1.0"
~~~

## Usage

Declare a [`Feed`](https://becompany.github.io/akka-social-stream/latest/api/#ch.becompany.social.Feed) for Twitter tweets and GitHub events. Stream the latest 10 and all subsequent status messages (see [`Status`](https://becompany.github.io/akka-social-stream/latest/api/#ch.becompany.social.Status) class for details):


~~~ scala
val feed = Feed(
  "twitter" -> new TwitterFeed(Some("my_screen_name")),
  "github" -> new GithubFeed("my_github_organization")
)(10)
~~~

~~~ scala
feed.subscribe.runForeach { case (network, status) =>
  println(s"Status update on $network by ${status.author.name}")
}
~~~

## API documentation

* [Latest](http://becompany.github.io/akka-social-stream/latest/api/)
