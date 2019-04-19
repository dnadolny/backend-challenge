val ScalatraVersion = "2.6.5"

organization := "com.dnadolny.ada"

name := "ada-backend-challenge"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.12"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container,compile",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.sorm-framework" % "sorm" % "0.3.21",
  "com.h2database" % "h2" % "1.4.199",
  
  "joda-time" % "joda-time" % "2.10.1",
  
  "org.json4s" %% "json4s-jackson" % "3.5.2"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)
