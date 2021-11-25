name := "template_project"
version := "0.1"
scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  // Akka Libraries
  "com.typesafe.akka" %% "akka-stream" % "2.5.32",
  "com.typesafe.akka" %% "akka-actor" % "2.5.32",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  // Requesting and processing data
  "com.lihaoyi" %% "requests" % "0.6.9",
  "com.lihaoyi" %% "ujson" % "1.4.2"
)
