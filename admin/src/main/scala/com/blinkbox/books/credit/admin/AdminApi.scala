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

class AdminApi(adminService: AdminService, authenticator: BearerTokenAuthenticator) extends v2.JsonSupport with StrictLogging {

  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
        ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val route = monitor(logger, throwableMarshaller) {
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
                  entity(as[DebitRequest]) { debitRequest =>
                    if (debitRequest.amount.value <= BigDecimal(0)) {
                      complete(StatusCodes.BadRequest, v2.Error("InvalidAmount", None))
                    } else if (debitRequest.amount.currency != "GBP") {
                      complete(StatusCodes.BadRequest, v2.Error("UnsupportedCurrency", None))
                    } else if (adminService.hasRequestAlreadyBeenProcessed(debitRequest.requestId)) {
                      complete(StatusCodes.NoContent)
                    } else {
                      onComplete(adminService.addDebit(userId, debitRequest.amount, debitRequest.requestId)) {
                        case Success(_) => complete(StatusCodes.NoContent)
                        case Failure(ex: InsufficientFundsException) => complete(StatusCodes.BadRequest, v2.Error("InsufficientFunds", None))
                        case Failure(ex) => complete(StatusCodes.InternalServerError)
                      }
                    }
                  }
                }
              } ~
                path("credits") {
                  authenticateAndAuthorize(authenticator.withElevation(Critical), hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { implicit adminUser =>
                    entity(as[CreditRequest]) { credit =>
                      if (credit.amount.value <= BigDecimal(0)) {
                        complete(StatusCodes.BadRequest, v2.Error("InvalidAmount", None))
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
      } ~
        path("accountcredit" / "reasons") {
          get {
            complete(StatusCodes.OK, ReasonResponse(adminService.getCreditReasons()))
          }
        }
    }
  }
}

case class DebitRequest(amount: Money, requestId: String)
case class CreditRequest(amount: Money, requestId: String)
case class ReasonResponse(reasons: List[String])