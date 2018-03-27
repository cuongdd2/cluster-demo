name := "cluster-demo"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.11"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.2"
libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.21.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

