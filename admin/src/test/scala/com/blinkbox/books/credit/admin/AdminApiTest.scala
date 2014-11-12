package com.blinkbox.books.credit.admin

import com.blinkbox.books.test.MockitoSyrup
import org.scalatest.FlatSpec
import org.mockito.Mockito._
import spray.http.StatusCodes
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService with MockitoSyrup {

  def actorRefFactory = system
  val creditHistory = CreditHistoryRepository.dummy
  val creditHistoryRepository = mock[CreditHistoryRepository]

  val route = (new AdminApi(creditHistoryRepository)).route

  "AdminApi" should "200 on credit history request" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(123)).thenReturn(Some(creditHistory))
    Get("/admin/users/123/credit") ~> route ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  it should "404 on unknown user" in {
    when(creditHistoryRepository.lookupCreditHistoryForUser(666)).thenReturn(None)
    Get("/admin/users/666/credit") ~> route ~> check {
      assert(status == StatusCodes.NotFound)
    }
  }

  it should "404 on unexpected path" in {
    Get("/nope") ~> route ~> check {
      assert(handled == false)
    }
  }

}