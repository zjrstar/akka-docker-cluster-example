package com.mlh.clustering

import akka.actor._
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import sample.ReplicatedCache
import sample.ReplicatedCache.{Cached, GetFromCache, PutInCache}

import scala.concurrent.Await
import scala.concurrent.duration._

object ClusteringApp extends App {

  import com.mlh.clustering.ClusteringConfig._

  implicit val timeout = Timeout(3 second)

  implicit val system = ActorSystem(clusterName)

  val clusterListener = system.actorOf(Props[ClusterListener], name = "clusterListener")

  // Create an actor that handles cluster domain events
  val replicatedCache = system.actorOf(Props[ReplicatedCache], name = "replicatedCache")

  replicatedCache ! PutInCache("key1", s"A: ${System.currentTimeMillis()}")

  while (true) {
    val f = replicatedCache ? GetFromCache("key1")
    val result = Await.result(f, 30 seconds).asInstanceOf[Cached]
    result match {
      case Cached(key, Some(value)) => //println(s"""key=${key} for value=${value}""")
    }
    Cluster(system).state.members.filter(_.address.host.get.contains("seed")).foreach(m => println(s"seed member: ${m}"))
    Cluster(system).state.members.foreach(m => println(s"member: ${m}"))
    Thread.sleep(5000)
  }

  sys.addShutdownHook(system.terminate())
}
