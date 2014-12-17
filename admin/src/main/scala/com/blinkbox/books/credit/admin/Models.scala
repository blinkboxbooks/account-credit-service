package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

case class CreditBalance (
  id: Option[Int],
  requestId : String,
  value: BigDecimal,
  transactionType: TransactionType.TransactionType,
  reason: Option[Reason.Reason],
  createdAt: DateTime,
  updatedAt: Option[DateTime],
  customerId:Int,
  adminUserId: Option[Int]
)

object CreditBalanceFactory {
  def fromCredit(requestId: String, value: BigDecimal, reason: Reason.Reason, customerId: Int, adminUserId: Int): CreditBalance =
    CreditBalance(None, requestId, value, TransactionType.Credit, Some(reason), new DateTime(), None, customerId, Some(adminUserId))

  def fromDebit(requestId: String, value: BigDecimal, customerId: Int): CreditBalance =
    CreditBalance(None, requestId, value, TransactionType.Debit, None, new DateTime(), None, customerId, None)
}

object TransactionType extends Enumeration {
  type TransactionType = Value
  val Credit, Debit = Value
}

object Reason extends Enumeration {
  type Reason = Value
  val GoodwillBookIssue, GoodwillTechnicalIssue, GoodwillServiceIssue, GoodwillCustomerRetention, StaffCredit, CreditVoucherCode, Hudl2Promotion = Value
}

