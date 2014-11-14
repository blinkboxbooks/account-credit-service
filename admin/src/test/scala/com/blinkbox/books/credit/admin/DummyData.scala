package com.blinkbox.books.credit.admin

import org.json4s.JsonAST.JObject

object DummyData {
  import org.json4s.JsonDSL._

  val expectedForCsm: JObject =
    ("netBalance" ->
      ("amount" -> 1000) ~
      ("currency" -> "GBP")) ~
    ("history" -> List(
      ("type" -> "credit") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
        (("amount" -> 1000) ~
        ("currency" -> "GBP"))) ~
      ("reason" ->
        ("reason" -> "Why not?")) ~
      ("issuer" ->
        (("name" -> "James Bond") ~
        ("roles" -> List(
          ("name" -> "csr"))))),

      ("type" -> "debit") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
      ("amount" -> 1000) ~
      ("currency" -> "GBP"))))

  var expectedForCsr: JObject =
    ("netBalance" ->
      ("amount" -> 1000) ~
        ("currency" -> "GBP")) ~
      ("history" -> List(
        ("type" -> "credit") ~
          ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
          ("amount" ->
            (("amount" -> 1000) ~
              ("currency" -> "GBP"))) ~
          ("reason" ->
            ("reason" -> "Why not?")),

        ("type" -> "debit") ~
          ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
          ("amount" ->
            ("amount" -> 1000) ~
              ("currency" -> "GBP"))))
}
