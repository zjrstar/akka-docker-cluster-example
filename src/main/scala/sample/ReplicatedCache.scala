package sample

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import scala.concurrent.duration._

object ReplicatedCache {
  def props: Props = Props[ReplicatedCache]

  private final case class Request(key: String, replyTo: ActorRef)

  case class PutInCache(key: String, value: Any)

  case class Evict(key: String)

  case class GetFromCache(key: String)

  case class Cached(key: String, value: Option[Any])

}

class ReplicatedCache extends Actor with ActorLogging {

  import ReplicatedCache._
  import akka.cluster.ddata.Replicator._

  val replicator = DistributedData(context.system).replicator
  implicit val cluster = Cluster(context.system)

  def dataKey(entryKey: String): LWWMapKey[String, Any] = LWWMapKey("cache-" + math.abs(entryKey.hashCode) % 100)

  def receive: Receive = {
    case PutInCache(key, value) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteAll(timeout = 5 seconds))(_ + (key -> value))
    case Evict(key) =>
      replicator ! Update(dataKey(key), LWWMap(), WriteAll(timeout = 5 seconds))(_ - key)
    case GetFromCache(key) =>
      replicator ! Get(dataKey(key), ReadMajority(timeout = 5 seconds), Some(Request(key, sender())))
    case g@GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[_, _] => data.asInstanceOf[LWWMap[String, Any]].get(key) match {
          case Some(value) => {
            log.error("Get value {} fro key {}", value, key)
            replyTo ! Cached(key, Some(value))
          }
          case None => replyTo ! Cached(key, None)
        }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      replyTo ! Cached(key, None)
    case _: UpdateResponse[_] =>
  }

}
