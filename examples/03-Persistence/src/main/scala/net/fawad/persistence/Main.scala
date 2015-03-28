package net.fawad.persistence

import akka.actor.ActorDSL.{Act, actor}
import akka.actor._
import akka.routing.FromConfig

import scala.util.Random


object Main extends App {
  implicit val system = ActorSystem()
  val baristas = system.actorOf(FromConfig.props(Props[Barista]), "barista")
  val cashregister = system.actorOf(Props[CashRegister], "cashregister")

  val customer = actor(new Act {
    import akka.event.Logging
    val log = Logging(context.system, "customer")
    become {
      case GoOrderCoffee() => {
        //for(idx <- 0 until 10 ){
          baristas ! OrderCoffee(CoffeeOrder(Random.nextInt(9) + 1))
        //}
      }
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
