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
    import context.dispatcher
    context.system.scheduler.scheduleOnce(
      1 seconds,
      self,
      self ! StartTime(currentTime)
    )
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
  /*
  bar1: TestBar,
  bar2: TestBar,
  sBar: Seq[TestBar],
  mBar: Map[String, TestBar]
  */
)

object TestBar {

  def random = TestBar(
    Random.alphanumeric.take(Random.nextInt).mkString,
    "Lorem ipsum dolor set amet",
    Random.alphanumeric.take(100).mkString,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(Random.nextInt).mkString).toList,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(10).mkString -> Random.alphanumeric.take(Random.nextInt).mkString).toMap,
    Random.nextInt,
    Random.nextInt,
    Random.nextInt,    
    (1 to Random.nextInt(100)).toList,
    (1 to Random.nextInt(100)).map(i => i.toString -> i).toMap
  )

  implicit val format = Json.format[TestBar]

}

object TestFoo {

  def random = TestFoo(
    Random.alphanumeric.take(Random.nextInt).mkString,
    "Lorem ipsum dolor set amet",
    Random.alphanumeric.take(100).mkString,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(Random.nextInt).mkString).toList,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(Random.nextInt).mkString).toList,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(Random.nextInt).mkString).toList,
    (1 to Random.nextInt(100)).map(_ => Random.alphanumeric.take(10).mkString -> Random.alphanumeric.take(Random.nextInt).mkString).toMap,
    Random.nextInt,
    Random.nextInt,
    Random.nextInt,
    (1 to Random.nextInt(100)).toList,
    (1 to Random.nextInt(100)).map(i => i.toString -> i).toMap
    //TestBar.random,
    //TestBar.random,
    //(1 to 10).map(_ => TestBar.random).toList,
    //(1 to 10).map(i => i.toString -> TestBar.random).toMap
  )

  import TestBar.format
  implicit val format = Json.format[TestFoo]

}

case class TestBar(
  st1: String,
  st2: String,
  st3: String,
  sSt: Seq[String],
  mSt: Map[String, String],
  i1: Int,
  i2: Int,
  i3: Int,
  sI: Seq[Int],
  mI: Map[String, Int]
)

class JsonWorker extends Actor with ActorLogging {

  import CpuTime._
  import TestFoo._
  import TestBar._

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

