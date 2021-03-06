package worker

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}

class WorkResultConsumer extends Actor with ActorLogging {

  val mediator = DistributedPubSub(context.system).mediator
  mediator ! DistributedPubSubMediator.Subscribe(Master.ResultsTopic, self)

  def receive = {
    //收到Subscribe的Ack回应消息
    case _: DistributedPubSubMediator.SubscribeAck =>
    //收到WorkResult
    case WorkResult(workId, result) =>
      log.info("Consumed result: {}", result)
  }

}