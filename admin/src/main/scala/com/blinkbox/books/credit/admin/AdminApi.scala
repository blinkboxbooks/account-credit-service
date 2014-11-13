package com.blinkbox.books.credit.admin

import com.blinkbox.books.spray.v2
import org.joda.time.DateTime
import org.json4s.{Formats, ShortTypeHints}
import spray.routing._
import Directives._
import com.blinkbox.books.auth.User
import com.blinkbox.books.spray.AuthDirectives._
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._

class AdminApi(creditHistoryRepository: CreditHistoryRepository, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport {
  override implicit def jsonFormats = v2.JsonFormats.blinkboxFormat(ShortTypeHints(List(classOf[Debit], classOf[Credit])))

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
trait CreditOrDebit
case class Debit(dateTime: DateTime, amount: Money) extends CreditOrDebit
case class Credit(dateTime: DateTime, amount: Money, reason: CreditReason, issuer: CreditIssuer) extends CreditOrDebit
case class CreditIssuer(name: String, roles: Set[UserRole])
case class CreditHistory(netBalance: Money, history: List[CreditOrDebit])
