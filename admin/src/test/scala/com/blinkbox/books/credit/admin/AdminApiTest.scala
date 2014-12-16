package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.{ UserRole, User, Elevation }
import com.blinkbox.books.spray.v2
import com.blinkbox.books.test.MockitoSyrup
import org.joda.time.DateTime
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
import com.blinkbox.books.spray.BearerTokenAuthenticator
import spray.testkit.ScalatestRouteTest
import scala.concurrent.Future
import org.mockito.Matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService with MockitoSyrup with v2.JsonSupport with BeforeAndAfter {

  def actorRefFactory = system
  implicit val routeTestTimeout = RouteTestTimeout(5.second)

  "AdminApi" should "200 on credit history request for known user as Customer Service Representative" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(DummyData.expectedForCsr == responseAs[JValue])
      assert(status == StatusCodes.OK)
    }
  }

  it should "200 on credit history request for known user as Customer Service Manager" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSM)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(DummyData.expectedForCsm == responseAs[JValue])
      assert(status == StatusCodes.OK)
    }
  }

  it should "not show issuer information to Customer Service Representatives" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(!containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "show issuer information to Customer Service Managers" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSM)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "show issuer information to users with Customer Service Representative /and/ Customer Service Manager roles" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCsmAndCsr)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(containsIssuerInformation(responseAs[JObject]))
    }
  }

  it should "return v2 media type on credit history request" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.lookupCreditHistoryForUser(123)).thenReturn(Future.successful(creditHistory))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(mediaType == `application/vnd.blinkbox.books.v2+json`)
    }
  }

  it should "200 on credit history request for unknown user" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.lookupCreditHistoryForUser(666)).thenReturn(Future.successful(AdminApiTest.zeroCreditHistory))
    Get("/admin/users/666/accountcredit") ~> route ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  it should "401 on credit history request when passed incorrect credentials" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))))
    Get("/admin/users/123/accountcredit") ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
      verifyNoMoreInteractions(adminService)
    }
  }

  it should "404 on unexpected path" in new TestFixture {
    Get("/nope") ~> route ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  it should "204 on add debit endpoint, as Customer Service Manager and Customer Service Representative" in new TestFixture {
    Set(authenticatedUserCSR, authenticatedUserCSM).foreach { adminUser =>
      when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(adminUser)))
      val amount = Money(BigDecimal.valueOf(90.01), "GBP")
      when(adminService.addDebit(123, amount, "good")).thenReturn(Future.successful(()))
      Post("/admin/users/123/accountcredit/debits", debitRequest) ~> route ~> check {
        verify(adminService).addDebit(123, amount, "good")
        assert(status == StatusCodes.NoContent)
      }
      reset(adminService)
    }
  }

  it should "400 on add debit endpoint, if trying to debit more credit than they have" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.addDebit(any[Int], any[Money], any[String])).thenReturn(Future.failed(new InsufficientFundsException))
    Post("/admin/users/123/accountcredit/debits", debitRequest) ~> route ~> check {
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
  it should "204 on add debit endpoint, if requestId has previously succeeded, even if the debit is more than currently available" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    when(adminService.hasRequestAlreadyBeenProcessed("good")).thenReturn(true)
    Post("/admin/users/123/accountcredit/debits", debitRequest) ~> route ~> check {
      verify(adminService, never()).addDebit(any[Int], any[Money], any[String])
      assert(status == StatusCodes.NoContent)
    }
  }

  it should "400 on add debit endpoint, if trying to debit non-GBP" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    Post("/admin/users/123/accountcredit/debits", nonGbpDebitRequest) ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("UnsupportedCurrency"))
    }
  }

  it should "400 on add debit endpoint, if trying to debit a zero amount" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    val zeroCreditRequest = DebitRequest(Money(BigDecimal.valueOf(0), "GBP"), "good")
    Post("/admin/users/123/accountcredit/debits", zeroCreditRequest) ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("InvalidAmount"))
    }
  }

  it should "400 on add debit endpoint, if trying to debit a negative amount" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    val negativeCreditRequest = DebitRequest(Money(BigDecimal.valueOf(-1), "GBP"), "good")
    Post("/admin/users/123/accountcredit/debits", negativeCreditRequest) ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
      assert(responseAs[JObject] == errorMessage("InvalidAmount"))
    }
  }

  it should "401 on add debit endpoint, with no auth" in new TestFixture  {
   when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, List()))))
    Post("/admin/users/123/accountcredit/debits") ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
    }
  }

  it should "401 on add debit endpoint, with invalid credentials" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))))
    Post("/admin/users/123/accountcredit/debits") ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
      verifyNoMoreInteractions(adminService)
    }
  }

  it should "403 on add debit endpoint, with unauthorised credentials" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserWithoutRequiredRoles)))
    Post("/admin/users/123/accountcredit/debits") ~> route ~> check {
      assert(status == StatusCodes.Forbidden)
      verifyNoMoreInteractions(adminService)
    }
  }

  it should "400 on add debit endpoint, if missing body" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserCSR)))
    Post("/admin/users/123/accountcredit/debits") ~> route ~> check {
      assert(status == StatusCodes.BadRequest)
    }
  }

  // add Credit tests 

  it should "204 NoContent on add Credit endpoint, as Customer Service Manager and Customer Service Representative" in new TestFixture {
    Set(authenticatedUserCSR, authenticatedUserCSM).foreach { adminUser =>
      val amount = Money(BigDecimal.valueOf(90.01), "GBP")
      val creditRequest = CreditRequest(amount, "tests125455")

      when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(adminUser)))
      when(adminService.addCredit(creditRequest, 12)(adminUser)).thenReturn(Future.successful(()))

      Post("/admin/users/12/accountcredit/credits", creditRequest) ~> route ~> check {
        verify(adminService).addCredit(CreditRequest(amount, "tests125455"), 12)(adminUser)
        assert(status == StatusCodes.NoContent)
      }
    }
  }

  it should "401 when add credit request with incorrect credentials" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))))
    Post("/admin/users/12/accountcredit/credits") ~> route ~> check {
      assert(status == StatusCodes.Unauthorized)
      verifyNoMoreInteractions(adminService)
    }
  }

  it should "403 on add credit request, without the required roles" in new TestFixture {
    when(authenticator.apply(any[RequestContext])).thenReturn(Future.successful(Right(authenticatedUserWithoutRequiredRoles)))
    Post("/admin/users/123/accountcredit/credits") ~> route ~> check {
      assert(status == StatusCodes.Forbidden)
      verifyNoMoreInteractions(adminService)
    }
  }

  it should "200 on get credit reasons" in new TestFixture {
    when(adminService.getCreditReasons()).thenReturn(List("foo", "bar"))
    Get("/admin/accountcredit/reasons") ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(DummyData.expectedForCreditReasons == responseAs[JValue])
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

object AdminApiTest {
  val dummy = {
  val thePast = new DateTime(2012,1,2,3,4,5)
  val cheap = Money(BigDecimal.valueOf(1000.53))
  val requestId = "sdfnaksfniofgniaodoir84t839t"
  val credits = List(Credit(requestId, thePast, cheap, CreditReason.CreditVoucherCode, CreditIssuer("James Bond", Set(UserRole.CustomerServicesRep))))
  val debits = List(Debit(requestId, thePast, cheap))
  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
  val eithers = (credits ++ debits).sortBy {
    case Debit(rq, dt, _) => dt
    case Credit(rq, dt, _, _, _) => dt
  }
  CreditHistory(cheap, eithers)
}

  val zeroCreditHistory = CreditHistory(Money(BigDecimal(0)), List())
}

class TestFixture extends v2.JsonSupport with MockitoSyrup {

  val adminService = mock[AdminService]
  val authenticator: BearerTokenAuthenticator = mock[BearerTokenAuthenticator]
  val creditHistory = AdminApiTest.dummy

  val api = new AdminApi(adminService, authenticator)
  val route = api.route

  override implicit def jsonFormats = api.jsonFormats

  val accessToken = "accessToken123"

  val authenticatedUserCSR = User(accessToken, claims = Map("sub" -> "urn:blinkbox:zuul:user:1", "bb/rol" -> Array(UserRole.CustomerServicesRep)))
  val authenticatedUserCSM = User(accessToken, claims = Map("sub" -> "urn:blinkbox:zuul:user:1", "bb/rol" -> Array(UserRole.CustomerServicesManager)))
  val authenticatedUserCsmAndCsr = User(accessToken, claims = Map("sub" -> "urn:blinkbox:zuul:user:1", "bb/rol" -> Array(UserRole.CustomerServicesManager, UserRole.CustomerServicesRep)))
  val authenticatedUserWithoutRequiredRoles = User(accessToken, claims = Map("sub" -> "urn:blinkbox:zuul:user:2"))

  when(authenticator.withElevation(Elevation.Critical)).thenReturn(authenticator)

  val debitRequest = DebitRequest(Money(BigDecimal.valueOf(90.01), "GBP"), "good")
  val nonGbpDebitRequest = DebitRequest(Money(BigDecimal.valueOf(90.01), "USD"), "good")
}