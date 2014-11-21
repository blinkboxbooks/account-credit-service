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

trait AdminApi extends  v2.JsonSupport { 
  implicit val adminService: AdminService
  import adminService._
  val creditHistoryRepository: CreditHistoryRepository
  val  authenticator: ContextAuthenticator[User]
  override implicit def jsonFormats = {
    val typeHints =
      ShortTypeHints(List()) +
      ExplicitTypeHints(Map(classOf[DebitForRendering] -> "debit", classOf[CreditForRendering] -> "credit"))
    v2.JsonFormats.blinkboxFormat(typeHints)
  }
  

  val route = get {
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
  }
  
   private lazy val createVoucherCampaign =get {
    pathPrefix("admin" / "users" / IntNumber) { customerId =>
      path("accountcredit"/ "credits") {
        authenticateAndAuthorize(authenticator, hasAnyRole(CustomerServicesRep, CustomerServicesManager)) { adminUser =>
          entity(as[AddCreditRequest]) { req =>
            complete(addCredit(req, adminUser,customerId))
          }
        }
      }
    }
  }
  
}

