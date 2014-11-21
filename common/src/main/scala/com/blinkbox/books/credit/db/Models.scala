package com.blinkbox.books.credit.db

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

object TransactionType extends Enumeration {
  type TransactionType = Value
  val Credit, Debit = Value
}

object Reason extends Enumeration {
  type Reason = Value
  val GoodwillBookIssue, GoodwillTechnicalIssue, GoodwillServiceIssue, GoodwillCustomerRetention, CreditRefund, StaffCredit, CreditVoucherCode, Hudl2Promotion = Value
}
