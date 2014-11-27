package com.blinkbox.books.credit.admin

import com.blinkbox.books.json.ExplicitTypeHints
import com.blinkbox.books.spray.v2
import spray.http.StatusCodes
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import org.json4s.ShortTypeHints
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
import akka.dispatch.OnSuccess

class AdminApi(creditHistoryRepository: CreditHistoryRepository, adminService: AdminService, authenticator: ContextAuthenticator[User]) extends v2.JsonSupport with StrictLogging {

  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
        ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }

  val route = monitor(logger, throwableMarshaller) {
    get {
      pathPrefix("admin" / "users" / IntNumber) { userId =>
        path("accountcredit") {
          authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { user =>
            val issuerBehaviour = if (user.isInRole(UserRole.CustomerServicesManager)) keepIssuer _ else removeIssuer _
            complete(creditHistoryRepository.lookupCreditHistoryForUser(userId).map {
              case CreditHistory(m, h) => CreditHistoryForRendering(m, h.map(issuerBehaviour))
            })
          }
        }
      }
    } ~
      post {
        pathPrefix("admin" / "users" / IntNumber) { userId =>
          path("accountcredit" / "credits") {
            authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { implicit adminUser =>
              entity(as[Credit]) { credit =>
                if (credit.amount.amount == BigDecimal.valueOf(0) || credit.amount.amount < BigDecimal.valueOf(0)) {
                  complete(StatusCodes.BadRequest)
                } else {
                  onSuccess(adminService.addCredit(credit, userId)) { resp =>
                    complete(resp)
                  }
                }
              }
            }
          }
        }
      }
  }

}

