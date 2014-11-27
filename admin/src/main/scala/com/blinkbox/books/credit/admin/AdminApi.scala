package com.blinkbox.books.credit.admin

import com.blinkbox.books.json.ExplicitTypeHints
import com.blinkbox.books.spray.v2
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import org.json4s.ShortTypeHints
import spray.http.StatusCodes
import spray.routing._
import Directives._
import com.blinkbox.books.auth.{UserRole, User}
import com.blinkbox.books.spray.AuthDirectives._
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._
import com.blinkbox.books.credit.admin.RenderingFunctions._
import com.blinkbox.books.spray.MonitoringDirectives.monitor
import com.blinkbox.books.spray.v2.Implicits.throwableMarshaller

class AdminApi(creditHistoryRepository: CreditHistoryRepository, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport with StrictLogging {
  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
      ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val route = monitor(logger, throwableMarshaller) {
    pathPrefix("admin" / "users" / IntNumber) { userId =>
      path("accountcredit") {
        get {
          authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { adminUser =>
            val issuerBehaviour = if (adminUser.isInRole(UserRole.CustomerServicesManager)) keepIssuer _ else removeIssuer _
            complete(creditHistoryRepository.lookupCreditHistoryForUser(userId).map {
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(issuerBehaviour))
            })
          }
        } ~
        path("debits") {
          post {
            authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { adminUser =>
              entity(as[CreditRequest]) { creditRequest =>
                if (creditRequest.amount.amount <= BigDecimal.valueOf(0)) {
                  complete(StatusCodes.BadRequest, v2.Error("InvalidAmount", None))
                } else if (creditRequest.amount.currency != "GBP") {
                  complete(StatusCodes.BadRequest, v2.Error("UnsupportedCurrency", None))
                } else if (creditHistoryRepository.hasRequestAlreadyBeenProcessed(creditRequest.requestId)) {
                  complete(StatusCodes.NoContent)
                } else if (creditRequest.amount.amount > creditHistoryRepository.lookupCreditBalanceForUser(123).amount) {
                  complete(StatusCodes.BadRequest, v2.Error("InsufficientFunds", None))
                } else {
                  creditHistoryRepository.debit(userId, creditRequest.amount, creditRequest.requestId)
                  complete(StatusCodes.NoContent)
                }

              }
            }
          }
        }
      }
    }
  }
}

case class CreditRequest(amount: Money, requestId: String)
