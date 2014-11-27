package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.UserRole._
import org.joda.time.DateTime

case class Money(amount: BigDecimal, currency: String = "GBP")
case class CreditReason(reason: String)
sealed trait CreditOrDebit
case class Debit(requestId: String, dateTime: DateTime, amount: Money) extends CreditOrDebit
case class Credit(requestId: String, dateTime: DateTime, amount: Money, reason: CreditReason, issuer: CreditIssuer) extends CreditOrDebit
case class CreditIssuer(name: String, roles: Set[UserRole])
case class CreditHistory(netBalance: Money, history: List[CreditOrDebit])