package com.blinkbox.books.credit.admin

import akka.actor.{ ActorRefFactory, ActorSystem, Props }
import akka.util.Timeout
import com.blinkbox.books.auth.{ Elevation, ZuulElevationChecker, ZuulTokenDecoder, ZuulTokenDeserializer }
import com.blinkbox.books.config.{ AuthClientConfig, Configuration }
import com.blinkbox.books.logging.Loggers
import com.blinkbox.books.spray.{ BearerTokenAuthenticator, HealthCheckHttpService, HttpServer }
import com.blinkbox.books.spray.{BearerTokenAuthenticator, HealthCheckHttpService, HttpServer}
import com.typesafe.scalalogging.StrictLogging
import spray.can.Http
import spray.http.Uri.Path
import spray.routing.HttpServiceActor
import com.blinkbox.books.slick.MySQLDatabaseSupport
import com.blinkbox.books.config.{ DatabaseConfig, Configuration }
import scala.concurrent.duration._
import com.blinkbox.books.time.SystemClock

object Main extends App with Configuration with Loggers with StrictLogging {
  logger.info("Starting Account-credit-service-v2-admin")
  val system = ActorSystem("account-credit-service-v2-admin")
  val appConfig = AppConfig(config)
  implicit val ec = system.dispatcher
  val authenticator = {
    val keysFolder = appConfig.auth.keysDir.getAbsolutePath
    val sessionUri = appConfig.auth.sessionUrl.toString
    new BearerTokenAuthenticator(
      new ZuulTokenDeserializer( new ZuulTokenDecoder(keysFolder)),
      new ZuulElevationChecker(sessionUri)(system))
  }

  val dbComponent = new DefaultDatabaseComponent(appConfig.databaseConfig)

  val accountCreditStore = new DbAccountCreditStore[MySQLDatabaseSupport](dbComponent.db, dbComponent.tables, dbComponent.exceptionFilter, ec)

  val adminService = new DefaultAdminService(accountCreditStore, SystemClock)

  val service = system.actorOf(Props(classOf[AdminApiActor], new AdminApi(adminService, authenticator)))

  logger.info("App started")
  HttpServer(Http.Bind(service, interface = appConfig.interface, port = appConfig.port))(system, system.dispatcher, Timeout(10.seconds))
}

class AdminApiActor(adminApi: AdminApi) extends HttpServiceActor {

  val healthService = new HealthCheckHttpService {
    override val basePath: Path = Path("/")
    override implicit def actorRefFactory: ActorRefFactory = AdminApiActor.this.actorRefFactory
  }

  override def receive = runRoute(healthService.routes ~ adminApi.route)
}