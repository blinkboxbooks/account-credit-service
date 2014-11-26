package com.blinkbox.books.credit.admin

import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import com.blinkbox.books.auth.{Elevation, ZuulElevationChecker, ZuulTokenDecoder, ZuulTokenDeserializer}
import com.blinkbox.books.config.{AuthClientConfig, Configuration}
import com.blinkbox.books.logging.Loggers
import com.blinkbox.books.spray.{BearerTokenAuthenticator, HealthCheckHttpService, HttpServer}
import com.typesafe.scalalogging.StrictLogging
import spray.can.Http
import spray.http.Uri.Path
import spray.routing.HttpServiceActor
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

object Main extends App with Configuration with Loggers with StrictLogging {
  logger.info("App starting")
  val system = ActorSystem("account-credit-service-v2-admin")
  val appConfig = AppConfig(config)
  val authenticator = {
    val keysFolder = appConfig.auth.keysDir.getAbsolutePath
    val sessionUri = appConfig.auth.sessionUrl.toString
    new BearerTokenAuthenticator(
      new ZuulTokenDeserializer(
        new ZuulTokenDecoder(keysFolder)),
        new ZuulElevationChecker(sessionUri)(system),
        Elevation.Unelevated)
  }

  val service = system.actorOf(Props(classOf[AdminApiActor], new AdminApi(new CreditHistoryRepository, authenticator)))

  logger.info("App started")
  HttpServer(Http.Bind(service, interface = appConfig.interface, port=appConfig.port))(system, system.dispatcher, Timeout(10.seconds))
}

class AdminApiActor(adminApi: AdminApi) extends HttpServiceActor {

  val healthService = new HealthCheckHttpService {
    override val basePath: Path = Path("/")
    override implicit def actorRefFactory: ActorRefFactory = AdminApiActor.this.actorRefFactory
  }

  override def receive = runRoute(adminApi.route ~ healthService.routes)
}