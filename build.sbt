name := "measure-doc"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.puppycrawl.tools" % "checkstyle" % "7.1"
libraryDependencies += "com.puppycrawl.tools" % "checkstyle" % "7.1" % Test classifier "tests"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"