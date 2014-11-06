package com.blinkbox.books.credit

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import com.blinkbox.books.spray.{HealthCheckHttpService, HttpServer}
import spray.can.Http
import spray.http.Uri.Path
import spray.routing.HttpServiceActor

import scala.concurrent.duration._

object Main extends App {
  val system = ActorSystem("account-credit-service-v2-public")
  val service = system.actorOf(Props(classOf[PublicApiActor]))
  HttpServer(Http.Bind(service, interface = "127.0.0.1", port=9876))(system, system.dispatcher, Timeout(10.seconds))
}

class PublicApiActor extends HttpServiceActor {

  val healthService = new HealthCheckHttpService {
    override val basePath: Path = Path("/")
    override implicit def actorRefFactory: ActorRefFactory = PublicApiActor.this.actorRefFactory
  }

  override def receive = runRoute(healthService.routes)
}