package net.fawad.supervision

import akka.actor.{Actor, ActorLogging}

class NumberFinderSequential extends Actor with ActorLogging {
  def receive = {
    case req@Find(needle, head+:tail) if(head == needle) =>
      log.info("Found {}!", needle)
      sender ! FindResult(req, true)
    case Find(needle, head+:tail)  =>
      Thread.sleep(2)
      log.debug("{} doesn't match {}. Continuing", needle, head)
      self forward Find(needle, tail)
    case req@Find(needle, Nil) =>
      log.info("Not Found {}", needle)
      sender ! FindResult(req, false)
  }
}