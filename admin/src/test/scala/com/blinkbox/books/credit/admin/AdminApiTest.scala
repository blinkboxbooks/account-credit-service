package com.blinkbox.books.credit.admin

import com.blinkbox.books.test.MockitoSyrup
import org.scalatest.FlatSpec
import spray.http.StatusCodes
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService with MockitoSyrup {

  def actorRefFactory = system
  val creditHistoryRepository = mock[CreditHistoryRepository]

  val route = (new AdminApi(creditHistoryRepository)).route

  "AdminApi" should "200 on credit history request" in {
    Get("/admin/users/123/credit") ~> route ~> check {
      assert(status == StatusCodes.OK)
    }
  }

  it should "404 on unexpected path" in {
    Get("/nope") ~> route ~> check {
      assert(handled == false)
    }
  }

}