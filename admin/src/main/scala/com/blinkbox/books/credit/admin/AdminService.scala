package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.credit.db._
import org.joda.time.DateTime
import scala.concurrent.Future
import com.blinkbox.books.time.SystemClock
import com.blinkbox.books.auth.UserRole
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit]
  def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit]
  def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory]
  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean
}

class DefaultAdminService(accountCreditStore: AccountCreditStore) extends AdminService {

  def nowTime = SystemClock.now()

  def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit] =
    lookupCreditHistoryForUser(userId).map{ _.netBalance }.map { currentBalance: Money => {
      val newBalance = currentBalance.value - amount.value
      val insufficientFunds = newBalance < 0
      if (insufficientFunds)
        throw new InsufficientFundsException
      else
        accountCreditStore.addDebit(CreditBalance(None, requestId, amount.value, TransactionType.Debit, None, nowTime, None, userId, None))
    }
  }

  def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory] = Future {
    val history = accountCreditStore.getCreditHistoryForUser(userId).map { h: CreditBalance =>
      if (h.transactionType == TransactionType.Debit)
        Debit(h.requestId, h.createdAt, Money(h.value))
      else
        Credit(h.requestId, h.createdAt, Money(h.value), CreditReason.CreditRefund, CreditIssuer(h.adminUserId.get.toString, Set()))
    }

    val netBalance: BigDecimal = history.foldLeft(BigDecimal(0))((cumulative: BigDecimal, e: CreditOrDebit) => e match {
      case Credit(_, _, a, _, _) => cumulative + a.value
      case Debit(_, _, a) => cumulative - a.value
    })

    CreditHistory(Money(netBalance), history.toList)
  }

  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean =
    accountCreditStore.getCreditBalanceByRequestId(requestId).nonEmpty

  override def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit] = Future {
    accountCreditStore.addCredit(copyAddCreditReqToCreditBalance(req, customerId, adminUser))
  }

  private def copyAddCreditReqToCreditBalance(req: Credit, customerId: Int, adminUser: User): CreditBalance = CreditBalance(
    id = None,
    requestId = req.requestId,
    value = req.amount.value,
    transactionType = TransactionType.Credit,
    reason = Some(creditReasonMapping(req.reason)),
    createdAt = nowTime,
    updatedAt = None,
    customerId = customerId,
    adminUserId = Some(adminUser.id))

  private def getAdminUserRoles(user: User): Set[String] = {
    user.roles.map(r => r.toString())
  }
  
  /**
   * Maps Spray layer Credit reason Enum to Database layer Reason Enum
   */
  private def creditReasonMapping(creditReason: CreditReason.Reason): Reason.Reason = {
    
    creditReason  match {
      case CreditReason.CreditRefund => Reason.CreditRefund
      case CreditReason.CreditVoucherCode => Reason.CreditVoucherCode
      case CreditReason.GoodwillBookIssue => Reason.GoodwillBookIssue
      case CreditReason.GoodwillCustomerRetention => Reason.GoodwillCustomerRetention
      case CreditReason.GoodwillServiceIssue => Reason.GoodwillServiceIssue
      case CreditReason.GoodwillTechnicalIssue => Reason.GoodwillTechnicalIssue
      case CreditReason.Hudl2Promotion => Reason.Hudl2Promotion
      case CreditReason.StaffCredit => Reason.StaffCredit
      case _ => throw new Exception("Invalid Reason")
    }
  }
}