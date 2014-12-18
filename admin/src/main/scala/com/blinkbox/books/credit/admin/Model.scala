package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.UserRole._
import org.joda.time.DateTime

case class Money(value: BigDecimal, currency: String = "GBP")
sealed trait CreditOrDebit
case class Debit(requestId: String, dateTime: DateTime, amount: Money) extends CreditOrDebit
case class Credit(requestId: String, dateTime: DateTime, amount: Money, reason: CreditReason.Reason, issuer: CreditIssuer) extends CreditOrDebit
case class CreditIssuer(name: String, roles: Set[UserRole])
case class CreditHistory(netBalance: Money, history: List[CreditOrDebit])

object CreditHistory {
  def buildFromCreditBalances(cbs: Seq[CreditBalance]): CreditHistory = {
    val history = cbs.map { cb: CreditBalance =>
      if (cb.transactionType == TransactionType.Debit)
        Debit(cb.requestId, cb.createdAt, Money(cb.value))
      else
        Credit(cb.requestId, cb.createdAt, Money(cb.value), CreditReason.CreditVoucherCode, CreditIssuer(cb.adminUserId.get.toString, Set()))
    }

    val netBalance = history.foldLeft(BigDecimal(0))((cumulativeAmount, creditOrDebit) => creditOrDebit match {
      case Credit(_, _, amount, _, _) => cumulativeAmount + amount.value
      case Debit(_, _, amount) => cumulativeAmount - amount.value
    })

    CreditHistory(Money(netBalance), history.toList)
  }
}

object CreditReason extends Enumeration {
  type Reason = Value
  val GoodwillBookIssue, GoodwillTechnicalIssue, GoodwillServiceIssue, GoodwillCustomerRetention, StaffCredit, CreditVoucherCode, Hudl2Promotion = Value
}