// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

// Use the Play sbt plugin for Play projects
//addSbtPlugin("play" % "sbt-plugin" % "2.1.3")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.0-RC1")

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.6")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

//addSbtPlugin("io.spray" % "sbt-twirl" % "0.6.2" from "file:///~/.ivy2/local/io.spray/sbt-twirl/scala_2.10/sbt_0.13/0.6.2/jars/sbt-twirl.jar")
