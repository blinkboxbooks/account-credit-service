package com.blinkbox.books.credit.admin

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import com.blinkbox.books.config.Configuration
import com.blinkbox.books.logging.Loggers
import com.blinkbox.books.spray.{HealthCheckHttpService, HttpServer}
import com.typesafe.scalalogging.slf4j.StrictLogging
import spray.can.Http
import spray.http.Uri.Path
import spray.routing.HttpServiceActor

import scala.concurrent.duration._

object Main extends App with Configuration with Loggers with StrictLogging {
  logger.info("App starting")
  val system = ActorSystem("account-credit-service-v2-admin")
  val service = system.actorOf(Props(classOf[AdminApiActor]))

  val interface = config.getString("admin.listen.address")
  val port = config.getInt("admin.listen.port")

  logger.info("App started")
  HttpServer(Http.Bind(service, interface = interface, port=port))(system, system.dispatcher, Timeout(10.seconds))
}

class AdminApiActor extends HttpServiceActor {

  val healthService = new HealthCheckHttpService {
    override val basePath: Path = Path("/")
    override implicit def actorRefFactory: ActorRefFactory = AdminApiActor.this.actorRefFactory
  }

  override def receive = runRoute(healthService.routes)
}