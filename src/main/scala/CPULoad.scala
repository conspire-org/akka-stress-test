package com.goconspire.stresstest

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging

import scala.compat.Platform
import scala.concurrent.duration._
import scala.util.Random

object CpuTime {

  case class StartTime(time: Long)
  case object Done

}

class CpuTime extends Actor with ActorLogging {

  import CpuTime._

  val maxTime = context.system.settings.config.getLong("stresstest.cpu.max-time")
  val step = context.system.settings.config.getLong("stresstest.cpu.step")
  val factor = context.system.settings.config.getInt("stresstest.cpu.factor")

  var countdown = factor
  var currentTime = 1L

  override def preStart = {
    log.info("Starting CPU time test")
    kickoff(currentTime)
  }

  def kickoff(next: Long) = {
    self ! StartTime(next)
  }

  def receive = {
    case s: StartTime =>
      countdown = factor
      log.info(s"Creating CPU time of ${s.time} millis...")
      (1 to factor) foreach { i =>
        context.actorOf(Props[CpuTimeWorker]) ! s
      }
    case Done =>
      countdown -= 1
      if(countdown <= 0) {
        kickoff(currentTime + step)
        currentTime += step
      }
  }

}

class CpuTimeWorker extends Actor with ActorLogging {

  import CpuTime._

  def receive = {
    case StartTime(time) =>
      val x = Array.fill(1000000)(Random.nextInt)
      val stopTime = Platform.currentTime + time
      log.info(s"Working for $time millis")
      while(Platform.currentTime < stopTime) {
        x.sorted
      }
      log.info(s"Completed time of ${time} millis")
      sender ! Done
  }

}
