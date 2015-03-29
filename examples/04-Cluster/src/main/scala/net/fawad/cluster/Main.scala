package net.fawad.cluster

import akka.actor.ActorDSL.{Act, actor}
import akka.actor._
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import net.fawad.persistence._

import scala.io.StdIn
import scala.util.Random


object Main extends App {
  val port = if (args.isEmpty) "0" else args(0)
  println(s"Listening to port ${port}")
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("CoffeeSystem", config)
  val baristas = system.actorOf(FromConfig.props(Props[Barista]), "barista")
  val cashregister = system.actorOf(Props[CashRegister], "cashregister")

  val customer = actor(new Act {

    import akka.event.Logging

    val log = Logging(context.system, "customer")
    become {
      case GoOrderCoffee() => {
        for (idx <- 0 until 10) {
          baristas ! OrderCoffee(CoffeeOrder(Random.nextInt(9) + 1))
        }
      }
      case GiveCoffee(order) =>
        log.info("Yaay, I haz my coffee {}", order)
        baristas ! PayForCoffee(order, order.amount)
      case "Thank you" => log.info("This is me enjoying my coffee on my way out")
    }
  })
  while(StdIn.readLine("Press enter to submit Order") != "q"){
    customer ! GoOrderCoffee()
  }
  system.shutdown()
}
