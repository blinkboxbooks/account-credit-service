package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.spray.v2
import com.blinkbox.books.test.MockitoSyrup
import org.json4s.JsonAST.JObject
import org.json4s._
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.mockito.Mockito._
import spray.http.HttpHeaders.Authorization
import spray.http.{ OAuth2BearerToken, StatusCodes }
import spray.routing.authentication.{ContextAuthenticator, Authentication}
import spray.routing._
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.authentication.{ ContextAuthenticator, Authentication }
import spray.routing.{ Route, AuthenticationFailedRejection, RequestContext, HttpService }
import com.blinkbox.books.spray.v2.`application/vnd.blinkbox.books.v2+json`
import spray.testkit.ScalatestRouteTest
import scala.concurrent.Future
import org.mockito.Matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService with MockitoSyrup with v2.JsonSupport with BeforeAndAfter {

  def actorRefFactory = system
  val creditHistory = CreditHistoryRepository.dummy
  val creditHistoryRepository = mock[CreditHistoryRepository]
  val adminService = mock[AdminService]
  val authenticator = new StubAuthenticator

  val api = new AdminApi(creditHistoryRepository, adminService, authenticator)
  val route = api.route

  override implicit def jsonFormats = api.jsonFormats

  val creditRequest = CreditRequest(Money(BigDecimal.valueOf(90.01), "GBP"), "good")
  val nonGbpCreditRequest = CreditRequest(Money(BigDecimal.valueOf(90.01), "USD"), "good")

  val csrAuth: Authorization = Authorization(OAuth2BearerToken("csr"))
  val csmAuth: Authorization = Authorization(OAuth2BearerToken("csm"))
  var csmAndCsrAuth: Authorization = Authorization(OAuth2BearerToken("csr,csm"))
  val invalidAuth: RequestTransformer = Authorization(OAuth2BearerToken("invalid"))

  val oneMillionPounds = Money(BigDecimal.valueOf(1000000))

  before {
    reset(creditHistoryRepository)
  }

  "AdminApi" should "200 on credit history request for known user as CSR" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csrAuth ~> route ~> check {
      assert(DummyData.expectedForCsr == responseAs[JValue])
      assert(status == StatusCodes.OK)
    }
  }

  it should "200 on credit history request for known user as CSM" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csmAuth ~> route ~> check {
      assert(DummyData.expectedForCsm == responseAs[JValue])
      assert(status == StatusCodes.OK)
    }
  }

  it should "not show issuer information to CSRs" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csrAuth ~> route ~> check {
      assert(!containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "show issuer information to CSMs" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csmAuth ~> route ~> check {
      assert(containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "show issuer information to users with CSR /and/ CSM roles" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csmAndCsrAuth ~> route ~> check {
      assert(containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "return v2 media type on credit history request" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(mediaType == `application/vnd.blinkbox.books.v2+json`)
    }
  }

  it should "404 on credit history request for unknown user" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(666)).thenReturn(None)
    Get("/admin/users/666/accountcredit") ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  it should "401 on credit history request when passed incorrect credentials" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/accountcredit") ~> invalidAuth ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  it should "404 on unexpected path" in {
    Get("/nope") ~> route ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  it should "204 on add debit endpoint, as CSR" in {
    val amount = Money(BigDecimal.valueOf(90.01), "GBP")
    when(creditHistoryRepository.debit(123, amount, "good")).thenReturn(true)
    Post("/admin/users/123/accountcredit/debits", creditRequest) ~> csrAuth ~> route ~> check {
      verify(creditHistoryRepository).debit(123, amount, "good")
      assert(status == StatusCodes.NoContent)
    }
  }

  it should "204 on add debit endpoint, as CSM" in {
    val amount = Money(BigDecimal.valueOf(90.01), "GBP")
    when(creditHistoryRepository.debit(123, amount, "good")).thenReturn(true)
    Post("/admin/users/123/accountcredit/debits", creditRequest) ~> csmAuth ~> route ~> check {
      verify(creditHistoryRepository).debit(123, amount, "good")
      assert(status == StatusCodes.NoContent)
    }
  }

  it should "400 on add debit endpoint, if trying to debit more credit than they have" in {
    when(creditHistoryRepository.debit(any[Int], any[Money], any[String])).thenReturn(false)
    Post("/admin/users/123/accountcredit/debits", creditRequest) ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("InsufficientFunds"))
    }
  }

  /*
   * Suppose the following requests occur:
   *
   * Row name | Request                  | Response code | Net balance after request
   * -------- | ------------------------ | ------------- | -------------------------
   * A        | credit(£10, requestId=1) | 204           | £10
   * B        | debit(£10, requestId=2)  | 204           | £0
   * C        | debit(£1, requestId=3)   | 400           | £0    // 400 due to insufficient funds
   * D        | debit(£10, requestId=2)  | 204           | £0
   *
   * D returns 204, which on first inspection seems odd, since the debit is for more than the user's balance.
   * This is expected, however, due to the idempotence of the debit endpoint.
   *
   * The rationale is that we return 204:
   *    * when a debit request we haven't seen before is successfully applied (e.g. B)
   *    * whenever we notice that particular debit (uniquely identified by the requestId) has already been applied
   *
   */
  it should "204 on add debit endpoint, if requestId has previously succeeded, even if the debit is more than currently available" in {
    when(creditHistoryRepository.hasRequestAlreadyBeenProcessed("good")).thenReturn(true)
    Post("/admin/users/123/accountcredit/debits", creditRequest) ~> csrAuth ~> route ~> check {
      verify(creditHistoryRepository, never()).debit(any[Int], any[Money], any[String])
      assert(status == StatusCodes.NoContent)
    }
  }

  it should "400 on add debit endpoint, if trying to debit non-GBP" in {
    Post("/admin/users/123/accountcredit/debits", nonGbpCreditRequest) ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("UnsupportedCurrency"))
    }
  }

  it should "400 on add debit endpoint, if trying to debit a zero amount" in {
    val zeroCreditRequest = CreditRequest(Money(BigDecimal.valueOf(0), "GBP"), "good")
    Post("/admin/users/123/accountcredit/debits", zeroCreditRequest) ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("InvalidAmount"))
    }
  }

  it should "400 on add debit endpoint, if trying to debit a negative amount" in {
    val negativeCreditRequest = CreditRequest(Money(BigDecimal.valueOf(-1), "GBP"), "good")
    Post("/admin/users/123/accountcredit/debits", negativeCreditRequest) ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("InvalidAmount"))
    }
  }

  it should "401 on add debit endpoint, with no auth" in {
    Post("/admin/users/123/accountcredit/debits") ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  it should "401 on add debit endpoint, with invalid credentials" in {
    Post("/admin/users/123/accountcredit/debits") ~> invalidAuth ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  it should "403 on add debit endpoint, with unauthorised credentials" in {
    val unauthorisedAuth: Authorization = Authorization(OAuth2BearerToken("unauthorised"))
    Post("/admin/users/123/accountcredit/debits") ~> unauthorisedAuth ~> route ~> check {
      assert(status == StatusCodes.Forbidden)
    }
  }

  it should "400 on add debit endpoint, if missing body" in {
    Post("/admin/users/123/accountcredit/debits") ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  def containsIssuerInformation(j: JValue): Boolean = {
    val issuerInfo: List[List[JField]] = for {
      JObject(child) <- j
      JField("issuer", JObject(issuer)) <- child
    } yield issuer

    !issuerInfo.flatten.isEmpty
  }

  def errorMessage(code: String): JObject = {
    import org.json4s.JsonDSL._
    ("code" -> code)
  }

}

class StubAuthenticator extends ContextAuthenticator[User] {
  override def apply(v1: RequestContext): Future[Authentication[User]] = Future {
    v1.request.headers.filter(_.name == "Authorization") match {
      case List(authHeader) => {
        val rolesInRequest: Set[String] = authHeader.value.substring("Bearer ".length).split(',').toSet
        val allowedRoles = Set("csr", "csm", "unauthorised")
        val validAuthentication = allowedRoles.intersect(rolesInRequest).nonEmpty
        if (validAuthentication)
          Right(User(1, Some(1), "foo", Map("bb/rol" -> rolesInRequest.toList)))
        else
          Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
      }
      case List() => Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, List()))
    }

  }(scala.concurrent.ExecutionContext.Implicits.global)
}
