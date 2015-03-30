package net.fawad.http

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.model.HttpMethods._
import akka.http.model._
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink

import scala.io.StdIn

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorFlowMaterializer()

  val reqHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path(path), _, _, _) => HttpResponse(entity = HttpEntity(MediaTypes.`text/html`, s"Hello ,${path}"))
  }
  val serverSource = Http(system).bind(interface = "localhost", port = 9899)
  val bindingFuture = serverSource.to(Sink.foreach { connection =>
    connection.handleWithSyncHandler(reqHandler)
  }).run()
  StdIn.readLine()
}
