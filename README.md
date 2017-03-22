# Akka Social Stream

Generate a feed of social network status messages as Akka stream.

## Installation

Add the dependency to your SBT build file:

~~~ sbt
libraryDependencies += "ch.becompany" %% "akka-social-stream" % "0.1.1"
~~~

## Usage

Declare a [`Feed`](https://becompany.github.io/akka-social-stream/latest/api/#ch.becompany.social.Feed) for Twitter tweets, GitHub events and Facebook page events, including the latest 10 messages:

~~~ scala
val updateInterval = 5 minutes
val feed = Feed(
  "twitter" -> new TwitterFeed("my_screen_name", updateInterval),
  "github" -> new GithubFeed("my_github_organization", updateInterval)
  "facebook" -> new FacebookFeed("my_facebook_page_id", updateInterval)
)(10)
~~~

The parameters are the ID for the feed provider and the **updateInterval** as [`FiniteDuration`](http://www.scala-lang.org/api/current/scala/concurrent/duration/FiniteDuration.html). The **updateInterval** parameter is optional and by default the maximum allowed rate per provider is configured. Values that result in a higher update rate than the allowed by the providers are silently ignored.

Subscribe to the stream of status messages (see [`Status`](https://becompany.github.io/akka-social-stream/latest/api/#ch.becompany.social.Status) class for details):

~~~ scala
feed.subscribe.runForeach { case (network, date, status) =>
  println(s"Status update on $network by ${status.author.name}")
}
~~~

## API documentation

* [Latest](http://becompany.github.io/akka-social-stream/latest/api/)
