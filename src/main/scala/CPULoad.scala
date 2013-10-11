package com.goconspire.stresstest

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging

import play.api.libs.json._

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
    self ! StartTime(currentTime)
  }

  def kickoff(next: Long) = {
    self ! StartTime(next)
  }

  def receive = {
    case s: StartTime =>
      countdown = factor
      log.info(s"Creating CPU time of ${s.time} millis...")
      (1 to factor) foreach { i =>
        context.actorOf(Props[JsonWorker].withDispatcher("my-dispatcher")) ! s
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
      val x = Array.fill(1000000)(Random.nextInt(100))
      val stopTime = Platform.currentTime + time
      log.info(s"Working for $time millis")
      while(Platform.currentTime < stopTime) {
        x.sorted
      }
      log.info(s"Completed time of ${time} millis")
      sender ! Done
  }

}

case class TestFoo(
  st1: String,
  st2: String,
  st3: String,
  sSt1: Seq[String],
  sSt2: Seq[String],
  sSt3: Seq[String],
  mSt: Map[String, String],
  i1: Int,
  i2: Int,
  i3: Int,
  sI: Seq[Int],
  mI: Map[String, Int]
)

object TestFoo {

  def random = TestFoo(
    "Lorem ipsum dolor set amet",
    "Lorem ipsum dolor set amet",
    "Lorem ipsum dolor set amet",
    (1 to 100).map(i => i.toString).toList,
    (1 to 100).map(i => i.toString).toList,
    (1 to 100).map(i => i.toString).toList,
    (1 to 100).map(i => i.toString -> "asdf").toMap,
    100,
    100,
    100,
    (1 to 100).toList,
    (1 to 100).map(i => i.toString -> i).toMap
  )

  implicit val format = Json.format[TestFoo]

}

class JsonWorker extends Actor with ActorLogging {

  import CpuTime._
  import TestFoo._

  def receive = {
    case StartTime(num) =>
      log.info(s"Serialize/deserialize $num times")
      (1l to num) foreach { _ =>
        val json = Json.toJson(TestFoo.random)
        val foo = Json.fromJson(json)(TestFoo.format)
      }
      log.info(s"Completed ops of ${num} times")
      sender ! Done
  }

}

