package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.credit.db._
import scala.concurrent.Future
import com.blinkbox.books.time.Clock
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit]
  def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit]
  def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory]
  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean
}

class DefaultAdminService(accountCreditStore: AccountCreditStore, clock: Clock) extends AdminService {

  override def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit] =
    lookupCreditHistoryForUser(userId).map { creditHistory =>
      val newBalance = creditHistory.netBalance.value - amount.value
      val insufficientFunds = newBalance < 0
      if (insufficientFunds)
        throw new InsufficientFundsException
      else
        accountCreditStore.addDebit(CreditBalanceFactory.fromDebit(requestId, amount.value, userId))
  }

  override def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory] = Future {
    val history = accountCreditStore.getCreditHistoryForUser(userId).map { cb: CreditBalance =>
      if (cb.transactionType == TransactionType.Debit)
        Debit(cb.requestId, cb.createdAt, Money(cb.value))
      else
        Credit(cb.requestId, cb.createdAt, Money(cb.value), CreditReason.CreditRefund, CreditIssuer(cb.adminUserId.get.toString, Set()))
    }

    val netBalance = history.foldLeft(BigDecimal(0))((cumulativeAmount, creditOrDebit) => creditOrDebit match {
      case Credit(_, _, amount, _, _) => cumulativeAmount + amount.value
      case Debit(_, _, amount) => cumulativeAmount - amount.value
    })

    CreditHistory(Money(netBalance), history.toList)
  }

  override def hasRequestAlreadyBeenProcessed(requestId: String): Boolean =
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
    createdAt = clock.now(),
    updatedAt = None,
    customerId = customerId,
    adminUserId = Some(adminUser.id))

  private def getAdminUserRoles(user: User): Set[String] = user.roles.map(r => r.toString())
  
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