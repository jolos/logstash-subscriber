name := "akka-zmq"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "spray repo" at "http://repo.spray.cc/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"

libraryDependencies += "com.typesafe.akka" % "akka-zeromq" % "2.0.1"

libraryDependencies += "play" % "play_2.9.1" % "2.0.1"

libraryDependencies += "net.databinder" %% "dispatch-core" % "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-http" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-oauth" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-nio" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-http" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-jsoup" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-http-json" % "0.8.7"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.1"
 
