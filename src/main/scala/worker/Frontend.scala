package worker

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Frontend {

  def props: Props = Props(classOf[Frontend])

  case class Ok(result: Int)

  case object NotOk

  private final case class Request(key: String, replyTo: ActorRef)

  case class PutInCache(key: String, value: Int)

  case class Evict(key: String)

  case class GetFromCache(key: String)

  case class Cached(key: String, value: Option[Int])

}

class Frontend extends Actor with ActorLogging {

  import Frontend._

  val replicator = DistributedData(context.system).replicator
  implicit val cluster = Cluster(context.system)

  val master = context.actorOf(Master.props(10.seconds), "master")

  def dataKey(entryKey: String): LWWMapKey[String, Int] = LWWMapKey("cache-" + math.abs(entryKey.hashCode) % 100)

  def receive = {
    case work: Work =>
      val key = work.job.toString
      replicator ! Get(dataKey(key), ReadMajority(timeout = 5 seconds), Some(Request(key, sender())))
    case g@GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[_, _] => data.asInstanceOf[LWWMap[String, Int]].get(key) match {
          case Some(value) => {
            log.error("Get value {} fro key {}", value, key)
            replyTo ! Ok(value)
          }
          case None => replyTo ! NotOk
        }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      implicit val timeout = Timeout(5.seconds)
      (master ? Work("", key.toInt)) map {
        case Master.Ack(_) => replyTo ! Ok(100)
      } recover { case _ => NotOk }
    case _: UpdateResponse[_] =>
  }

}