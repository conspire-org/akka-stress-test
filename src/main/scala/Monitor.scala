package com.goconspire.stresstest

import akka.actor._
import akka.cluster._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

import scala.compat.Platform
import scala.concurrent.duration._

class Monitor extends Actor with ActorLogging {

  override def preStart = {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
  }

  override def postStop = {
    writeReport
    Cluster(context.system).unsubscribe(self)
  }

  var memberInfo = Map.empty[Member, List[ClusterDomainEvent]].withDefaultValue(List.empty[ClusterDomainEvent])

  def receive = {
    case state: CurrentClusterState ⇒ // ignore
    case event: MemberUp ⇒
      log.info("Member is Up: {}", event.member.address)
      memberInfo += event.member -> (memberInfo(event.member) :+ event)
    case event: UnreachableMember ⇒
      log.info("Member detected as unreachable: {}", event.member)
      memberInfo += event.member -> (memberInfo(event.member) :+ event)
    case event: MemberExited ⇒
      log.info("Member detected as exited: {}", event.member)
      memberInfo += event.member -> (memberInfo(event.member) :+ event)
    case event: MemberRemoved =>
      log.info("Member is Removed: {} after {}",
        event.member.address, event.previousStatus)
      memberInfo += event.member -> (memberInfo(event.member) :+ event)
    case _: ClusterDomainEvent ⇒ // ignore
  }

  def writeReport = {
    val clusterConfig = io.Source.fromFile("src/main/resources/application.conf").mkString("\n")
    val report = memberInfo.mkString("\n")

    val output = "Config settings:\n\n" + clusterConfig + "\n\n" + report

    import sys.process._
    output #> new java.io.File("report-" + Platform.currentTime) !
  }

}
