package com.blinkbox.books.credit.admin

import com.blinkbox.books.json.ExplicitTypeHints
import com.blinkbox.books.spray.v2
import spray.http.StatusCodes
import com.typesafe.scalalogging.StrictLogging
import org.json4s.ShortTypeHints
import spray.http.StatusCodes
import spray.routing._
import Directives._
import com.blinkbox.books.auth.{ UserRole, User }
import com.blinkbox.books.spray.AuthDirectives._
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._
import com.blinkbox.books.credit.admin.RenderingFunctions._
import com.blinkbox.books.spray.MonitoringDirectives.monitor
import com.blinkbox.books.spray.BearerTokenAuthenticator
import com.blinkbox.books.spray.v2.Implicits.throwableMarshaller
import com.blinkbox.books.auth.Elevation.Critical
import spray.routing.authentication.ContextAuthenticator
import scala.util.Success
import scala.util.Failure
import scala.util.control.NonFatal

class AdminApi(adminService: AdminService, authenticator: BearerTokenAuthenticator) extends v2.JsonSupport with StrictLogging {

  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
        ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: InvalidRequestException => complete(StatusCodes.BadRequest, v2.Error(e.message, None))
    case e: InsufficientFundsException => complete(StatusCodes.BadRequest, v2.Error("InsufficientFunds", None))
    case NonFatal(e) =>
      logger.warn("an unknown exception occurred", e)
      complete(StatusCodes.ServerError, v2.Error("server_error", None))
  }

  val route = monitor(logger, throwableMarshaller) {
    handleExceptions(exceptionHandler) {
      pathPrefix("admin") {
        pathPrefix("users" / IntNumber) { userId =>
          pathPrefix("accountcredit") {
            pathEnd {
              get {
                authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { adminUser =>
                  val issuerBehaviour = if (adminUser.isInRole(UserRole.CustomerServicesManager)) keepIssuer _ else removeIssuer _
                  complete(adminService.lookupCreditHistoryForUser(userId).map {
                    case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(issuerBehaviour))
                  })
                }
              }
            } ~
              post {
                path("debits") {
                  authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { adminUser =>
                    entity(as[NewDebit]) { debitRequest =>
                      onSuccess(adminService.hasRequestAlreadyBeenProcessed(debitRequest.transactionId)) { alreadyProcessed =>
                        if (alreadyProcessed) {
                          complete(StatusCodes.NoContent)
                        } else {
                          onSuccess(adminService.addDebit(userId, debitRequest.amount, debitRequest.transactionId)) { resp =>
                            complete(StatusCodes.NoContent)
                          }
                        }
                      }
                    }
                  }
                } ~
                  path("credits") {
                    authenticateAndAuthorize(authenticator.withElevation(Critical), hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { implicit adminUser =>
                      entity(as[NewCredit]) { credit =>
                        onSuccess(adminService.hasRequestAlreadyBeenProcessed(credit.transactionId)) { alreadyProcessed =>
                          if (alreadyProcessed) {
                            complete(StatusCodes.NoContent)
                          } else {
                            onSuccess(adminService.addCredit(credit, userId)) { resp =>
                              complete(StatusCodes.NoContent)
                            }
                          }
                        }
                      }
                    }
                  }
              }
          }
        } ~
          path("accountcredit" / "reasons") {
            get {
              complete(StatusCodes.OK, ReasonResponse(adminService.getCreditReasons()))
            }
          }
      }
    }
  }
}

case class NewDebit(amount: Amount, transactionId: String)
case class NewCredit(amount: Amount, transactionId: String, reason: String)
case class ReasonResponse(reasons: List[String])
