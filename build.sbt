name := "akka-zmq"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"

libraryDependencies += "com.typesafe.akka" % "akka-zeromq" % "2.0.1"

libraryDependencies += "net.databinder" %% "dispatch-core" % "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-http" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-oauth" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-nio" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-http" %  "0.8.7"

libraryDependencies += "net.databinder" %% "dispatch-jsoup" %  "0.8.7"
 
