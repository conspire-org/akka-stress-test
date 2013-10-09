package com.goconspire.stresstest

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging

import scala.concurrent.duration._

object MemLoad {

  case class StartLoad(load: Int)

}

class MemLoad extends Actor with ActorLogging {

  import MemLoad._

  val maxLoad = context.system.settings.config.getInt("stresstest.mem.max-load")
  val step = context.system.settings.config.getInt("stresstest.mem.step")

  override def preStart = {
    import context.dispatcher
    (1 to maxLoad by step) foreach { load =>
      context.system.scheduler.scheduleOnce(
        step seconds,
        self,
        StartLoad(load)
      )
    }
  }

  def receive = {
    case s: StartLoad =>
      log.info("Creating load of ${s.load} bytes...")
      context.actorOf(Props[MemLoadWorker]) ! s
  }

}

class MemLoadWorker extends Actor with ActorLogging {

  import MemLoad._

  // Create $load bytes
  var x: Array[Byte] = Array.empty[Byte]
  // we need to keep a reference so the array isn't immediately GC'd

  def receive = {
    case StartLoad(load) =>
      x = Array.fill(load)(Byte.MaxValue)
      log.info("Created load of ${s.load} bytes")
  }

}
