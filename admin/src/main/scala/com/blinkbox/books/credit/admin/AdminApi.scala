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
import com.blinkbox.books.credit.admin.RenderingFunctions._

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
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(keepIssuer))
            })
          else if (user.isInRole(UserRole.CustomerServicesRep))
            complete(creditHistoryRepository.lookupCreditHistoryForUser(userId).map {
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(removeIssuer))
            })
          else
            throw new RuntimeException
        }
      }
    }
  }
}

