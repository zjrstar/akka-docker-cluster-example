package com.sample.cluster

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

class ClusterListener extends Actor with ActorLogging {

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    val cluster = Cluster(context.system)
    log.debug("starting up cluster listener...clusterId: ")
    cluster.subscribe(self, classOf[ClusterDomainEvent])
  }

  def receive = {
    case MemberJoined(member) =>
      log.debug("Member has joined: {}", member.address)
    case MemberWeaklyUp(member) =>
      log.debug("Member is weakly up {}", member.address)
    case state: CurrentClusterState =>
      log.debug("Current Leader is {}", state.getLeader)
      log.debug("Current members: {}", state.members.mkString(", "))
      log.debug("Current members role is : {}", state.allRoles.mkString(","))
    case MemberUp(member) =>
      log.debug("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.debug("Member detected as unreachable: {}", member)
    case MemberLeft(member) =>
      log.debug("Member is leaving {}", member.address)
    case MemberRemoved(member, previousStatus) =>
      log.debug("Member is Removed: {} after {}",
        member.address, previousStatus)
    case LeaderChanged(member) =>
      log.info("Leader changed: " + member)
    case any: MemberEvent =>
      log.info("Member Event: " + any.toString) // ignore
  }
}
