package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

sealed trait RenderingCreditOrDebit
case class CreditIssuerForRendering(name: String, roles: Set[String])
case class CreditForRendering(requestId: String,dateTime: DateTime, amount: Amount, reason: String, issuer: Option[CreditIssuerForRendering]) extends RenderingCreditOrDebit
case class DebitForRendering(requestId: String, dateTime: DateTime, amount: Amount) extends RenderingCreditOrDebit
case class CreditHistoryForRendering(balance: Amount, items: List[RenderingCreditOrDebit])

object RenderingFunctions {
  def removeIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(rq,dt, a, r, _) => CreditForRendering(rq, dt, a, r.toString(), None)
    case Debit(rq,dt, a) => DebitForRendering(rq,dt, a)
  }

  def keepIssuer(cd: CreditOrDebit): RenderingCreditOrDebit = cd match {
    case Credit(rq,dt, a, r, CreditIssuer(n, roles)) => CreditForRendering(rq,dt, a, r.toString(), Some(CreditIssuerForRendering(n, roles.map(_.toString))))
    case Debit(rq,dt, a) => DebitForRendering(rq,dt, a)
  }
}