# Akka Social Stream

Generate a feed of social network status messages as Akka stream.

## Installation

~~~
libraryDependencies += "ch.becompany" %% "akka-social-stream" % "0.1.0"
~~~

## Usage

~~~
val feed = new Feed(Map(
    "twitter" -> new TwitterFeed(Some("my_screen_name"))
  ))

// Stream the latest 10 and all subsequent status messages
feed.source(10).
  runForeach(println)
~~~


