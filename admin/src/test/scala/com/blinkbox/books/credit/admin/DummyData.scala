package com.blinkbox.books.credit.admin

import org.json4s.JsonAST.{JDecimal, JObject}

object DummyData {
  import org.json4s.JsonDSL._

  val expectedForCsm: JObject =
    ("balance" ->
      ("value" -> JDecimal(1000.53)) ~
      ("currency" -> "GBP")) ~
    ("items" -> List(
      ("type" -> "credit") ~
      ("transactionId" -> "sdfnaksfniofgniaodoir84t839t") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
        ("value" -> JDecimal(1000.53)) ~
        ("currency" -> "GBP")) ~
      ("reason" -> "CreditVoucherCode") ~
      ("issuer" ->
        ("name" -> "James Bond") ~
        ("roles" -> List("csr"))),

      ("type" -> "debit") ~
      ("transactionId" -> "sdfnaksfniofgniaodoir84t839t") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
        ("value" -> JDecimal(1000.53)) ~
        ("currency" -> "GBP"))))

  var expectedForCsr: JObject =
    ("balance" ->
      ("value" -> JDecimal(1000.53)) ~
      ("currency" -> "GBP")) ~
    ("items" -> List(
      ("type" -> "credit") ~
      ("transactionId" -> "sdfnaksfniofgniaodoir84t839t") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
        ("value" -> JDecimal(1000.53)) ~
        ("currency" -> "GBP")) ~
      ("reason" -> "CreditVoucherCode"),

      ("type" -> "debit") ~
      ("transactionId" -> "sdfnaksfniofgniaodoir84t839t") ~
      ("dateTime" -> "2012-01-02T03:04:05.000Z") ~
      ("amount" ->
        ("value" -> JDecimal(1000.53)) ~
        ("currency" -> "GBP"))))

  val expectedForCreditReasons: JObject = "reasons" -> List("foo", "bar")
}
