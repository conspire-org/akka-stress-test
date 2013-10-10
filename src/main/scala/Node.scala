package com.goconspire.stresstest
 
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.PoisonPill
import akka.kernel.Bootable
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterDomainEvent 
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.UnreachableMember

import scala.concurrent.duration._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class Node extends Bootable {

  val system = ActorSystem("ClusterSystem", ConfigFactory.load)

  def shutdown() {
    println("Shutting down ClusterSystem")
    system.shutdown()
  }
 
  def startup() {
    val roles = Cluster(system).selfRoles

    println("Node has " + roles.size + " roles")

    if(roles.contains("monitor"))
      startWorker("monitor")

    Cluster(system).registerOnMemberUp { 
      roles.filter(_ != "monitor") foreach { role =>
        startWorker(role)
      }
    }

  }

  def startWorker(role: String) = {
    getLoadActorClass(role) foreach { clazz =>
      println("Starting cluster node with role [" + role + "]")
      system.actorOf(
        Props.create(clazz).withDispatcher("my-dispatcher"), "worker"
      )
    }
  }

  def getLoadActorClass(role: String): Option[Class[_ <: Actor]] = {
    if(role equals "cpu")
      Some(classOf[CpuTime])
    else if(role equals "mem")
      Some(classOf[MemLoad])
    else if(role equals "monitor")
      Some(classOf[Monitor])
    else
      None
  }

}



