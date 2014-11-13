package com.blinkbox.books.credit.admin

import com.blinkbox.books.credit.admin.Serialisers.CreditHistorySerializer
import com.blinkbox.books.spray.v2
import org.joda.time.DateTime
import spray.routing._
import Directives._
import com.blinkbox.books.auth.{UserRole, Constraints, User}
import com.blinkbox.books.spray.AuthDirectives._
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._

class AdminApi(creditHistoryRepository: CreditHistoryRepository, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport {
  override implicit def jsonFormats = v2.JsonFormats.blinkboxFormat() + CreditHistorySerializer

  val route = get {
    pathPrefix("admin" / "users" / IntNumber) { userId =>
      path("credit") {
        authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { _ =>
          complete(creditHistoryRepository.lookupCreditHistoryForUser(userId))
        }
      }
    }
  }
}

case class Money(amount: BigDecimal, currency: String = "GBP")
case class CreditReason(reason: String)
case class Role(role: String)
case class Debit(dateTime: DateTime, amount: Money)
case class Credit(dateTime: DateTime, amount: Money, reason: CreditReason, issuer: CreditIssuer)
case class CreditIssuer(name: String, roles: Set[Role])
case class CreditHistory(netBalance: Money, history: List[Either[Debit, Credit]])

