package com.sample.cluster

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import sample.ReplicatedCache
import sample.ReplicatedCache.{Cached, GetFromCache, PutInCache}
import worker.Frontend.Ok
import worker._

import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

object ClusteringApp extends App with Protocol {

  import ClusteringConfig._

  implicit val timeout = Timeout(3 second)
  implicit val system = ActorSystem(clusterName)
  implicit val meterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // Create an actor that handles cluster domain events
  val clusterListener = system.actorOf(Props[ClusterListener], name = "clusterListener")

  val replicatedCache = system.actorOf(Props[ReplicatedCache], name = "replicatedCache")

  replicatedCache ! PutInCache("key1", s"A: ${System.currentTimeMillis()}")

  //  val master = system.actorOf(ClusterSingletonManager.props(Master.props(workTimeout), PoisonPill,
  //    ClusterSingletonManagerSettings(system).withRole("node")), "master")

  //  println(master.path.name)
  //
  //  val frontend = system.actorOf(ClusterSingletonManager.props(Frontend.props, PoisonPill,
  //    ClusterSingletonManagerSettings(system).withRole("node")), "frontend")

  val frontend = system.actorOf(Frontend.props, "frontend")
  system.actorOf(Props[WorkResultConsumer], "consumer")

  val route =
    path("square") {
      get {
        parameters(('id.as[String], 'number.as[Int]))
          .as(Work) { request =>
            implicit val timeout = Timeout(5.seconds)
            val work = Work(Random.nextString(5), request.job)
            onComplete(frontend ? work) {
              case Success(res: Ok) => complete(ToResponseMarshallable(OK -> res))
              case Failure(t) => complete(StatusCodes.InternalServerError, t.getMessage)
            }
          }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 9000)

//  while (true) {
//    val f = replicatedCache ? GetFromCache("key1")
//    val result = Await.result(f, 30 seconds).asInstanceOf[Cached]
//    result match {
//      case Cached(key, Some(value)) => //println(s"""key=${key} for value=${value}""")
//    }
//    Cluster(system).state.members.filter(_.address.host.get.contains("seed")).foreach(m => println(s"seed member: ${m}"))
//    Cluster(system).state.members.foreach(m => println(s"member: ${m}"))
//    Thread.sleep(5000)
//  }

  sys.addShutdownHook(system.terminate())
}
