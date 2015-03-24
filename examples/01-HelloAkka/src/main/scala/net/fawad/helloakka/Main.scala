package net.fawad.helloakka

import akka.actor.{Actor, Props, ActorSystem}


object Main extends App {
  val system = ActorSystem("hello")
  val greeter = system.actorOf(Props[Greeter])
  greeter ! "world"
  Thread.sleep(1000)
  system.shutdown()
}

class Greeter extends Actor {
  def receive = {
    case msg: String => println(s"Hello, ${msg}!")
  }
}