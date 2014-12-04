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

/* I'd like to name this 'CreditBalance', since Scala fanatics will have a heart attack if they see
 * word 'factory', despite these being factories.
 *
 * When I call this 'CreditBalance', however, slick fails to compile.
 */
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
  val GoodwillBookIssue, GoodwillTechnicalIssue, GoodwillServiceIssue, GoodwillCustomerRetention, CreditRefund, StaffCredit, CreditVoucherCode, Hudl2Promotion = Value
}

class InsufficientFundsException extends Exception