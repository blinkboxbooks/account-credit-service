package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.test.MockitoSyrup
import org.json4s._
import org.scalatest.FlatSpec
import org.mockito.Mockito._
import spray.http.HttpHeaders.Authorization
import spray.http.{OAuth2BearerToken, StatusCodes}
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.authentication.{ContextAuthenticator, Authentication}
import spray.routing.{Route, AuthenticationFailedRejection, RequestContext, HttpService}
import com.blinkbox.books.spray.v2.`application/vnd.blinkbox.books.v2+json`
import spray.testkit.ScalatestRouteTest
import org.json4s.jackson.JsonMethods._
import scala.concurrent.Future

class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService with MockitoSyrup {

  def actorRefFactory = system
  val creditHistory = CreditHistoryRepository.dummy
  val creditHistoryRepository = mock[CreditHistoryRepository]
  val authenticator = new StubAuthenticator

  val route: Route = (new AdminApi(creditHistoryRepository, authenticator)).route

  val csrAuth: Authorization = Authorization(OAuth2BearerToken("csr"))
  val csmAuth: Authorization = Authorization(OAuth2BearerToken("csm"))
  var csmAndCsrAuth: Authorization = Authorization(OAuth2BearerToken("csr,csm"))

  "AdminApi" should "200 on credit history request for known user as CSR" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csrAuth ~> route ~> check {
      assert(DummyData.expectedForCsr == parse(responseAs[String]))
      assert(status == StatusCodes.OK)
    }
  }

  it should "200 on credit history request for known user as CSM" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csmAuth ~> route ~> check {
      assert(DummyData.expectedForCsm == parse(responseAs[String]))
      assert(status == StatusCodes.OK)
    }
  }

  it should "not show issuer information to CSRs" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csrAuth ~> route ~> check {
      val json = parse(responseAs[String])
      val issuerInfo: List[List[JField]] = for {
        JObject(child) <- json
        JField("issuer", JObject(issuer)) <- child
      } yield issuer

      assert(issuerInfo.flatten.isEmpty)
    }
  }

  it should "show issuer information to CSMs" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csmAuth ~> route ~> check {
      val json = parse(responseAs[String])
      val issuerInfo: List[List[JField]] = for {
        JObject(child) <- json
        JField("issuer", JObject(issuer)) <- child
      } yield issuer

      assert(!issuerInfo.flatten.isEmpty)
    }
  }

  it should "show issuer information to users with CSR /and/ CSM roles" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csmAndCsrAuth ~> route ~> check {
      val json = parse(responseAs[String])
      val issuerInfo: List[List[JField]] = for {
        JObject(child) <- json
        JField("issuer", JObject(issuer)) <- child
      } yield issuer

      assert(!issuerInfo.flatten.isEmpty)
    }
  }

  it should "return v2 media type on credit history request" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(mediaType == `application/vnd.blinkbox.books.v2+json`)
    }
  }

  it should "404 on credit history request for unknown user" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(666)).thenReturn(None)
    Get("/admin/users/666/credit") ~> csrAuth ~> route ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  it should "401 on credit history request when passed incorrect credentials" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    val invalidAuth: RequestTransformer = Authorization(OAuth2BearerToken("invalid"))
    Get("/admin/users/123/credit") ~> invalidAuth ~> route ~> check {
      println(rejections)
      assert(rejection == AuthenticationFailedRejection(CredentialsRejected, List()))
    }
  }

  it should "404 on unexpected path" in {
    Get("/nope") ~> route ~> check {
      assert(handled == false)
    }
  }

}

class StubAuthenticator extends ContextAuthenticator[User] {
  override def apply(v1: RequestContext): Future[Authentication[User]] = Future {
    val authHeader = v1.request.headers.filter(_.name == "Authorization").head.value
    val rolesInRequest: Set[String] = authHeader.substring("Bearer ".length).split(',').toSet
    val allowedRoles = Set("csr", "csm")
    if (allowedRoles.intersect(rolesInRequest).nonEmpty)
      Right(User(1, Some(1), "foo", Map("bb/rol" -> rolesInRequest.toList)))
    else
      Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
  }(scala.concurrent.ExecutionContext.Implicits.global)
}