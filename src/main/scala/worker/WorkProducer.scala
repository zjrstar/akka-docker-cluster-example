package worker

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

object WorkProducer {
  case object Tick
}

class WorkProducer(frontend: ActorRef) extends Actor with ActorLogging {
  import WorkProducer._
  import context.dispatcher
  def scheduler = context.system.scheduler
  def rnd = ThreadLocalRandom.current
  def nextWorkId(): String = UUID.randomUUID().toString

  var n = 0

  override def preStart(): Unit = scheduler.scheduleOnce(5.seconds, self, Tick)

  // override postRestart so we don't call preStart and schedule a new Tick
  override def postRestart(reason: Throwable): Unit = ()

  def receive = {
    //接收发送tick给自己，然后发送work给frontend
    case Tick =>
      n += 1
      log.info("Produced work: {}", n)
      val work = Work(nextWorkId(), n)
      frontend ! work
      context.become(waitAccepted(work), discardOld = false)

  }

  def waitAccepted(work: Work): Actor.Receive = {
    //如果收到前端OK，等待3-10后发送Tick给自己
    case Frontend.Ok =>
      context.unbecome()
      scheduler.scheduleOnce(rnd.nextInt(3, 10).seconds, self, Tick)
    //如果收到前端NotOK，3秒后重新给前端发送work
    case Frontend.NotOk =>
      log.info("worker.Work not accepted, retry after a while")
      scheduler.scheduleOnce(3.seconds, frontend, work)
  }

}