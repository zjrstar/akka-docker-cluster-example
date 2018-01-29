name := "clustering"

organization := "com.mlh"

version := "0.3"

homepage := Some(url("https://github.com/mhamrah/clustering"))

startYear := Some(2013)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mhamrah/clustering"),
    "scm:git:https://github.com/mhamrah/clustering.git",
    Some("scm:git:git@github.com:mhamrah/clustering.git")
  )
)

/* scala versions and options */
scalaVersion := "2.11.4"

scalacOptions ++= Seq(
  "-deprecation"
  ,"-unchecked"
  ,"-encoding", "UTF-8"
  ,"-Xlint"
  ,"-Yclosure-elim"
  ,"-Yinline"
  ,"-Xverify"
  ,"-feature"
  ,"-language:postfixOps"
)

val akka = "2.5.8"

libraryDependencies ++= Seq (
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "ch.qos.logback" % "logback-classic" % "1.0.10",
  "com.typesafe.akka" %% "akka-testkit" % akka % "test",
  "com.typesafe.akka" %% "akka-actor" % akka,
  "com.typesafe.akka" %% "akka-cluster" % akka,
  "com.typesafe.akka" %% "akka-cluster-tools" % akka,
  "com.typesafe.akka" %% "akka-persistence" % akka,
  "com.typesafe.akka" %% "akka-slf4j"  % akka,
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.typesafe.akka" %% "akka-remote" % akka,
  "com.typesafe.akka" %% "akka-distributed-data" % akka,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akka,
  "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
  "commons-io" % "commons-io" % "2.4" % "test",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "com.typesafe" % "config" % "1.2.0"
)

maintainer := "Michael Hamrah <m@hamrah.com>"

dockerExposedPorts in Docker := Seq(1600)

//dockerEntrypoint in Docker := Seq("sh", "-c", "CLUSTER_IP=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1 }'` bin/clustering $*")
dockerEntrypoint in Docker := Seq("sh", "-c", "bin/clustering")

dockerRepository := Some("mhamrah")

dockerBaseImage := "10.232.128.157:5000/yiguan/java:1.7"
enablePlugins(JavaAppPackaging)
