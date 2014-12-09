package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import scala.concurrent.Future
import com.blinkbox.books.time.Clock
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def getCreditReasons(): List[String]
  def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit]
  def addCredit(req: CreditRequest, customerId: Int)(implicit adminUser: User): Future[Unit]
  def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory]
  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean
}

class DefaultAdminService(accountCreditStore: AccountCreditStore, clock: Clock) extends AdminService {

  override def getCreditReasons(): List[String] = CreditReason.values.toList.map { _.toString }

  override def addDebit(userId: Int, amount: Money, requestId: String): Future[Unit] = Future {
    accountCreditStore.addDebitIfUserHasSufficientCredit(userId, requestId, amount)
  }

  override def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory] = Future {
    CreditHistory.buildFromCreditBalances(accountCreditStore.getCreditHistoryForUser(userId))
  }

  override def hasRequestAlreadyBeenProcessed(requestId: String): Boolean =
    accountCreditStore.getCreditBalanceByRequestId(requestId).nonEmpty

  override def addCredit(req: CreditRequest, customerId: Int)(implicit adminUser: User): Future[Unit] = Future {
    accountCreditStore.addCredit(copyAddCreditReqToCreditBalance(req, customerId, adminUser))
  }

  private def copyAddCreditReqToCreditBalance(req: CreditRequest, customerId: Int, adminUser: User): CreditBalance = CreditBalance(
    id = None,
    requestId = req.requestId,
    value = req.amount.value,
    transactionType = TransactionType.Credit,
    reason = Some(Reason.CreditVoucherCode),
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