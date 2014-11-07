package com.blinkbox.books.credit.admin

import org.scalatest.FlatSpec
import spray.http.StatusCodes
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class AdminApiTest extends FlatSpec with ScalatestRouteTest with HttpService {

  def actorRefFactory = system

  val route = (new AdminApi()).route

  "The service" should "bar-admin on foo" in {
    Get("/admin/credit/foo") ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(responseAs[String] == "bar-admin")
    }
  }

}
