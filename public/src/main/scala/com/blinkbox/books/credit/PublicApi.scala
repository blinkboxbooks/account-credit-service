package com.blinkbox.books.credit

import spray.routing.Directives._
import spray.routing._

class PublicApi {
  val route = get {
    path("my" / "credit" / "foo") {
      complete("bar-public")
    }
  }
}
