package net.fawad.http

import akka.actor.{ActorLogging, Props, ActorRef, ActorSystem}
import akka.http.Http
import akka.http.model.HttpMethods._
import akka.http.model._
import akka.routing.FromConfig
import akka.stream.ActorFlowMaterializer
import akka.stream.actor.{ActorPublisher, MaxInFlightRequestStrategy, ActorSubscriber}
import akka.stream.scaladsl.{Source, Flow, Sink}
import net.fawad.persistence._

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Random

object CoffeeShop {
  def props: Props = Props(new CoffeeShop)
}
class CoffeeShop extends ActorSubscriber with ActorPublisher[String] with ActorLogging {
  val maxQueueSize = 10
  var queue = Map.empty[Int, ActorRef]
  implicit val system = ActorSystem()
  val baristas = system.actorOf(FromConfig.props(Props[Barista]), "barista")
  val cashregister = system.actorOf(Props[CashRegister], "cashregister")

  override val requestStrategy = new MaxInFlightRequestStrategy(max = maxQueueSize) {
    override def inFlightInternally: Int = queue.size
  }

  override def receive = {
    case GoOrderCoffee() => {
      baristas ! OrderCoffee(CoffeeOrder(Random.nextInt(9) + 1))
    }
    case GiveCoffee(order) =>
      log.info("Yaay, I haz coffee")
      baristas ! PayForCoffee(order, order.amount)
    case "Thank you" => log.info("This is me enjoying my coffee on my way out")
  }
}
object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorFlowMaterializer()
  implicit val ec = Implicits.global

  val reqHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/ordercoffee"), _, _, _) => Future(HttpResponse(entity = HttpEntity(MediaTypes.`text/html`, s"Preparing order")))
    case HttpRequest(GET, Uri.Path(path), _, _, _) => Future(HttpResponse(entity = HttpEntity(MediaTypes.`text/html`, s"Hello ,${path}")))
  }

  val serverSource = Http(system).bind(interface = "localhost", port = 9899)
  val cs = system.actorOf(Props[CoffeeShop])
  val procSubscriber = ActorSubscriber(cs)
  val procProducer = ActorPublisher(cs)

  serverSource.map(Flow(Sink(procSubscriber), Source(procProducer)))
  val bindingFuture = serverSource.to(Sink.foreach { connection =>
    connection.handleWithAsyncHandler(reqHandler)
  }).run()

  StdIn.readLine()
}
