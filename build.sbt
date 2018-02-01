import sbt.Keys.libraryDependencies
import com.trueaccord.scalapb.compiler.Version.scalapbVersion

name := "clustering"

organization := "com.mlh"

version := "0.3"

scalaVersion := "2.12.4"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

scalacOptions ++= Seq(
  "-deprecation"
  ,"-unchecked"
  ,"-encoding", "UTF-8"
  ,"-Xlint"
  ,"-Xverify"
  ,"-feature"
  ,"-language:postfixOps"
)

val akka = "2.5.8"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-testkit" % akka % "test",
  "com.typesafe.akka" %% "akka-actor" % akka,
  "com.typesafe.akka" %% "akka-cluster" % akka,
  "com.typesafe.akka" %% "akka-cluster-tools" % akka,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
  "com.typesafe.akka" %% "akka-persistence" % akka,
  "com.typesafe.akka" %% "akka-slf4j"  % akka,
  "com.typesafe.akka" %% "akka-remote" % akka,
  "com.typesafe.akka" %% "akka-distributed-data" % akka,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akka,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akka,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.11",
  "ch.qos.logback" % "logback-classic" % "1.0.10",
  "com.trueaccord.scalapb"      %% "scalapb-runtime"  % scalapbVersion  % "protobuf",
  "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "2.0.4",
  "com.github.scullxbones" %% "akka-persistence-mongo-rxmongo" % "2.0.4",
  "org.reactivemongo" %% "reactivemongo" % "0.12.5",
  "org.mongodb" % "casbah_2.12" % "3.1.1",
  "org.mongodb" % "mongodb-driver" % "3.4.2"
)

dockerExposedPorts in Docker := Seq(1600)

dockerEntrypoint := Seq("sh", "-c", "bin/clustering")

dockerRepository := Some("mhamrah")

dockerBaseImage := "java"
enablePlugins(JavaAppPackaging)
