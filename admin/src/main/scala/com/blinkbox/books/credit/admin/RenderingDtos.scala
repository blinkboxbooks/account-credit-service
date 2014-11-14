package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

trait RenderingCreditOrDebit
case class CreditForRendering(dateTime: DateTime, amount: Money, reason: String, issuer: Option[CreditIssuer]) extends RenderingCreditOrDebit
case class DebitForRendering(dateTime: DateTime, amount: Money) extends RenderingCreditOrDebit
case class CreditHistoryForRendering(balance: Money, items: List[RenderingCreditOrDebit])

object RenderingFunctions {
  def removeIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, _) => CreditForRendering(dt, a, r.reason, None)
    case Debit(dt, a) => DebitForRendering(dt, a)
  }

  def keepIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(dt, a, r, issuer) => CreditForRendering(dt, a, r.reason, Some(issuer))
    case Debit(dt, a) => DebitForRendering(dt, a)
  }
}