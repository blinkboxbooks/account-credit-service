package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

sealed trait RenderingCreditOrDebit
case class CreditIssuerForRendering(name: String, roles: Set[String])
case class CreditForRendering(dateTime: DateTime, amount: Money, reason: String, issuer: Option[CreditIssuerForRendering]) extends RenderingCreditOrDebit
case class DebitForRendering(dateTime: DateTime, amount: Money) extends RenderingCreditOrDebit
case class CreditHistoryForRendering(balance: Money, items: List[RenderingCreditOrDebit])

object RenderingFunctions {
  def removeIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, _) => CreditForRendering(dt, a, r.reason, None)
    case Debit(dt, a) => DebitForRendering(dt, a)
  }

  def keepIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, CreditIssuer(n, roles)) => CreditForRendering(dt, a, r.reason, Some(CreditIssuerForRendering(n, roles.map(_.toString))))
    case Debit(dt, a) => DebitForRendering(dt, a)
  }
}