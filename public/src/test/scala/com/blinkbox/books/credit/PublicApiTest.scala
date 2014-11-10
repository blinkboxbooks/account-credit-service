package com.blinkbox.books.credit

import org.scalatest.FlatSpec
import spray.http.StatusCodes
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class PublicApiTest extends FlatSpec with ScalatestRouteTest with HttpService {

  def actorRefFactory = system

  val route = (new PublicApi()).route

  "The service" should "bar-public on foo" in {
    Get("/my/credit/foo") ~> route ~> check {
      assert(status == StatusCodes.OK)
      assert(responseAs[String] == "bar-public")
    }
  }

}
