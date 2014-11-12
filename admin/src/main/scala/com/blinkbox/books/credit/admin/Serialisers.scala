package com.blinkbox.books.credit.admin

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JString, JValue}

object Serialisers {
  import org.json4s.JsonDSL._

  private implicit val dateTimeConverter: DateTime => JValue = dt => JString(ISODateTimeFormat.basicDateTimeNoMillis().print(dt))

  private implicit val moneyConverter: Money => JValue = money =>
    ("currency" -> money.currency) ~
      ("value" -> money.amount)

  private implicit val reasonConverter: CreditReason => JValue = cr => JString(cr.reason)

  implicit val eitherDebitCreditConverter: Either[Debit, Credit] => JValue = {
    case Left(Debit(dt, amount)) =>
      ("type" -> "debit") ~
        ("dateTime" -> dt) ~
        ("amount" -> amount)
    case Right(Credit(dt, amount, reason, issuer)) =>
      ("type" -> "credit") ~
        ("dateTime" -> dt) ~
        ("amount" -> amount) ~
        ("reason" -> reason) ~
        ("issuerName" -> issuer.name)
  }

  implicit val creditHistoryConverter: CreditHistory => JValue = ch =>
    ("amount" -> ch.netBalance) ~
    ("items" -> ch.history)

  // Needed to integrate with Spray. Eww.
  object CreditHistorySerializer extends CustomSerializer[CreditHistory](_ => ({
    case _ => throw new UnsupportedOperationException
  }, {
    case c: CreditHistory => creditHistoryConverter(c)
  }))
}
