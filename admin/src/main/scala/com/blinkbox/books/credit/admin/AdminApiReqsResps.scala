package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

case class AddCreditRequest(
  requestId: String,
  value: BigDecimal,
  reason: Reason.Reason)

object Reason extends Enumeration {
  type Reason = Value
  val GoodwillBookIssue, GoodwillTechnicalIssue, GoodwillServiceIssue, GoodwillCustomerRetention, CreditRefund, StaffCredit, CreditVoucherCode, Hudl2Promotion = Value
}

case class AddCreditResponse(
  requestId: String,
  amount: Money)