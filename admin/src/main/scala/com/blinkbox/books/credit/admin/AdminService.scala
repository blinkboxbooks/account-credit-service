package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import scala.concurrent.Future
import com.blinkbox.books.time.Clock
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def getCreditReasons(): List[String]
  def addDebit(userId: Int, amount: Amount, requestId: String): Future[Unit]
  def addCredit(req: NewCredit, customerId: Int)(implicit adminUser: User): Future[Unit]
  def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory]
  def hasRequestAlreadyBeenProcessed(requestId: String): Future[Boolean]
}

class DefaultAdminService(accountCreditStore: AccountCreditStore, clock: Clock) extends AdminService {

  override def getCreditReasons(): List[String] = Reason.values.toList.map(_.toString)

  override def addDebit(userId: Int, amount: Amount, requestId: String): Future[Unit] = Future {
    validateAmount(amount)
    accountCreditStore.addDebitIfUserHasSufficientCredit(userId, requestId, amount)
  }

  override def lookupCreditHistoryForUser(userId: Int): Future[CreditHistory] = Future {
    CreditHistory.buildFromCreditBalances(accountCreditStore.getCreditHistoryForUser(userId))
  }

  override def hasRequestAlreadyBeenProcessed(requestId: String): Future[Boolean] = Future {
    accountCreditStore.getCreditBalanceByRequestId(requestId).nonEmpty
  }

  override def addCredit(req: NewCredit, customerId: Int)(implicit adminUser: User): Future[Unit] = Future {
    accountCreditStore.addCredit(copyAddCreditReqToCreditBalance(req, customerId, adminUser))
  }

  private def copyAddCreditReqToCreditBalance(req: NewCredit, customerId: Int, adminUser: User): CreditBalance = {
    validateAmount(req.amount)
    CreditBalance(     
    id = None,
    transactionId = req.transactionId,
    value = req.amount.value,
    transactionType = TransactionType.Credit,
    reason = Some(creditReasonMapping(req.reason)),
    createdAt = clock.now(),
    updatedAt = None,
    customerId = customerId,
    adminUserId = Some(adminUser.id))
  }

  private def getAdminUserRoles(user: User): Set[String] = user.roles.map(r => r.toString())

  def creditReasonMapping(creditReason: String): Reason.Reason = {

    creditReason match {
      case "CreditVoucherCode"         => Reason.CreditVoucherCode
      case "GoodwillBookIssue"         => Reason.GoodwillBookIssue
      case "GoodwillCustomerRetention" => Reason.GoodwillCustomerRetention
      case "GoodwillServiceIssue"      => Reason.GoodwillServiceIssue
      case "GoodwillTechnicalIssue"    => Reason.GoodwillTechnicalIssue
      case "Hudl2Promotion"            => Reason.Hudl2Promotion
      case "StaffCredit"               => Reason.StaffCredit
      case _                           => throw new InvalidRequestException("invalid_reason")
    }
  }

  def validateAmount(amount: Amount) = {
    if (amount.asJodaMoney.isNegativeOrZero) throw new InvalidRequestException("invalid_amount")
  }
}