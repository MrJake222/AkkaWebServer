name := "http_queens_sbt"

version := "0.1"

scalaVersion := "2.13.8"

idePackagePrefix := Some("httpqueens")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.19"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"