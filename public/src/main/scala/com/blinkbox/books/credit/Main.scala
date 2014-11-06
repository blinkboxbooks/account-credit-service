package com.blinkbox.books.credit

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import com.blinkbox.books.config.Configuration
import com.blinkbox.books.spray.{HealthCheckHttpService, HttpServer}
import spray.can.Http
import spray.http.Uri.Path
import spray.routing.HttpServiceActor

import scala.concurrent.duration._

object Main extends App with Configuration {
  val system = ActorSystem("account-credit-service-v2-public")
  val service = system.actorOf(Props(classOf[PublicApiActor]))

  val interface = config.getString("admin.listen.address")
  val port = config.getInt("admin.listen.port")

  HttpServer(Http.Bind(service, interface = interface, port=port))(system, system.dispatcher, Timeout(10.seconds))
}

class PublicApiActor extends HttpServiceActor {

  val healthService = new HealthCheckHttpService {
    override val basePath: Path = Path("/")
    override implicit def actorRefFactory: ActorRefFactory = PublicApiActor.this.actorRefFactory
  }

  override def receive = runRoute(healthService.routes)
}