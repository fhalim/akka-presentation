package net.fawad.supervision

import akka.actor.ActorDSL._
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("hello")

  implicit val timeout = Timeout(5 seconds)

  //val calculator = system.actorOf(Props[NumberFinderSequential])
  val calculator = system.actorOf(Props[NumberFinderParallel], "calculator")

  val haystack = List.range(1, 100)
  implicit val i = inbox()

  {
    calculator ! Find(99, haystack)
    val result = i.receive().asInstanceOf[FindResult]
    println(s"Found 99: ${result.found}")
  }

  {
    calculator ! Find(-100, haystack)
    val result = i.receive().asInstanceOf[FindResult]
    println(s"Found -100: ${result.found}")
  }

  system.shutdown()
}

