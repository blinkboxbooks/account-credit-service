package com.blinkbox.books.credit.admin

import spray.routing._
import Directives._

class AdminApi {
  val route = get {
    path("admin" / "credit" / "foo") {
      complete("bar-admin")
    }
  }
}
