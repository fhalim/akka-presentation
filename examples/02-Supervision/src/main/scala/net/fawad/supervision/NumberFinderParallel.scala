package net.fawad.supervision

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class NumberFinderParallel extends Actor with ActorLogging {
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = context.dispatcher
  val workers = context.actorOf(FromConfig.props(Props[ParallelWorker]), "workers")

  def receive = {
    case Find(needle, haystack) =>
      val replyTo = sender
      log.info("Starting search")
      for (futures <- Future.sequence(for (elm <- haystack) yield (workers ? Find(needle, elm +: Nil)).mapTo[FindResult])) {
        replyTo ! futures.filter(_.found).headOption.getOrElse(futures.head)
      }
  }
}

class ParallelWorker extends Actor with ActorLogging {
  override def receive = {
    case req@Find(needle, head +: Nil) if (head == needle) =>
      Thread.sleep(2)
      log.info("Found {}!", needle)
      sender ! FindResult(req, true)
    case req@Find(needle, head +: Nil) =>
      if (Random.nextInt(100) == 0) {
        throw new Exception(s"Failed to process haystack value ${head}")
      }
      log.debug("Not found in {}", head)
      sender ! FindResult(req, false)
    case Crashed(msg) =>
      log.info("Reprocessing failed message {}", msg)
      self forward msg

  }

  override def preRestart(reason: Throwable, msg: Option[Any]): Unit = {
    for (payload <- msg) {
      self forward Crashed(payload)
    }
  }
}
