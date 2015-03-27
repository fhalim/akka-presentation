package net.fawad.persistence

import akka.actor.ActorDSL.{Act, actor}
import akka.actor._

import scala.util.Random




object Main extends App {
  implicit val system = ActorSystem()
  val baristas = system.actorOf(Props(classOf[Barista], "cashierbarista"), "barista")

  val customer = actor(new Act {
    import akka.event.Logging
    val log = Logging(context.system, "customer")
    become {
      case GoOrderCoffee() => baristas ! OrderCoffee(CoffeeOrder(Random.nextInt(10)))
      case GiveCoffee(order) =>
        log.info("Yaay, I haz coffee")
        baristas ! PayForCoffee(order, order.amount)
      case "Thank you" => log.info("This is me enjoying my coffee on my way out")
    }
  })
  customer ! GoOrderCoffee()
  Thread.sleep(1000)
  system.shutdown()
}
