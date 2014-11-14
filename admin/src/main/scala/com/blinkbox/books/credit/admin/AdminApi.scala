package com.blinkbox.books.credit.admin

import com.blinkbox.books.json.ExplicitTypeHints
import com.blinkbox.books.spray.v2
import org.joda.time.DateTime
import org.json4s.ShortTypeHints
import spray.routing._
import Directives._
import com.blinkbox.books.auth.{UserRole, User}
import com.blinkbox.books.spray.AuthDirectives._
import spray.routing.authentication.ContextAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global
import com.blinkbox.books.auth.UserRole._
import com.blinkbox.books.auth.Constraints._

class AdminApi(creditHistoryRepository: CreditHistoryRepository, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport {
  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
      ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val route = get {
    pathPrefix("admin" / "users" / IntNumber) { userId =>
      path("credit") {
        authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { user =>
          if (user.isInRole(UserRole.CustomerServicesManager))
            complete(creditHistoryRepository.lookupCreditHistoryForUser(userId).map {
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(Urgh.keepIssuer))
            })
          else if (user.isInRole(UserRole.CustomerServicesRep))
            complete(creditHistoryRepository.lookupCreditHistoryForUser(userId).map {
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(Urgh.removeIssuer))
            })
          else
            throw new RuntimeException
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


// Crap below
trait RenderingCreditOrDebit
case class CreditForRendering(dateTime: DateTime, amount: Money, reason: CreditReason, issuer: Option[CreditIssuer]) extends RenderingCreditOrDebit
case class DebitForRendering(dateTime: DateTime, amount: Money) extends RenderingCreditOrDebit
case class CreditHistoryForRendering(netBalance: Money, history: List[RenderingCreditOrDebit])

object Urgh {
  def removeIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, _) => CreditForRendering(dt, a, r, None)
    case Debit(dt, a) => DebitForRendering(dt, a)
  }

  def keepIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, issuer) => CreditForRendering(dt, a, r, Some(issuer))
    case Debit(dt, a) => DebitForRendering(dt, a)
  }
}