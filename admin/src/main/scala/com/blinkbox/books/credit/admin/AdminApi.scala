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
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._
import com.blinkbox.books.credit.admin.RenderingFunctions._
import com.blinkbox.books.spray.MonitoringDirectives.monitor
import com.blinkbox.books.spray.v2.Implicits.throwableMarshaller

class AdminApi(adminService: AdminService, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport with StrictLogging {

  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
        ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val route = monitor(logger, throwableMarshaller) {
    pathPrefix("admin" / "users" / IntNumber) { userId =>
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
                if (debitRequest.amount.amount <= BigDecimal(0)) {
                  complete(StatusCodes.BadRequest, v2.Error("InvalidAmount", None))
                } else if (debitRequest.amount.currency != "GBP") {
                  complete(StatusCodes.BadRequest, v2.Error("UnsupportedCurrency", None))
                } else if (adminService.hasRequestAlreadyBeenProcessed(debitRequest.requestId)) {
                  complete(StatusCodes.NoContent)
                } else if (adminService.debit(userId, debitRequest.amount, debitRequest.requestId)) {
                  complete(StatusCodes.NoContent)
                } else {
                  complete(StatusCodes.BadRequest, v2.Error("InsufficientFunds", None))
                }
              }
            }
          } ~
          path("credits") {
            authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { implicit adminUser =>
              entity(as[Credit]) { credit =>
                if (credit.amount.amount <= BigDecimal(0)) {
                  complete(StatusCodes.BadRequest)
                } else if (credit.amount.currency != "GBP") {
                  complete(StatusCodes.BadRequest)
                } else if (adminService.alreadyBeenProcessed(credit.requestId)) {
                  complete(StatusCodes.Created)
                } else {
                  onSuccess(adminService.addCredit(credit, userId)) { resp =>
                    complete(StatusCodes.Created)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

case class DebitRequest(amount: Money, requestId: String)
