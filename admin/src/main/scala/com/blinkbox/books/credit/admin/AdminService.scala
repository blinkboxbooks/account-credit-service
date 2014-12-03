package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.auth.UserRole
import com.blinkbox.books.credit.db._
import com.blinkbox.books.time.Clock
import com.blinkbox.books.time.TimeSupport
import scala.concurrent.Future
import com.blinkbox.books.time.SystemClock
import com.blinkbox.books.auth.UserRole
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit]

  def alreadyBeenProcessed(requestId: String): Boolean
}

class DefaultAdminService(accountCreditStore: AccountCreditStore) extends AdminService {

  val nowTime = SystemClock.now()

  override def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit] = Future {
    accountCreditStore.addCredit(copyAddCreditReqToCreditBalance(req, customerId, adminUser))
  }

  private def copyAddCreditReqToCreditBalance(req: Credit, customerId: Int, adminUser: User): CreditBalance = CreditBalance(
    id = None,
    requestId = req.requestId,
    value = req.amount.amount,
    transactionType = TransactionType.Credit,
    reason = Some(creditReasonMapping(req.reason)),
    createdAt = nowTime,
    updatedAt = None,
    customerId = customerId,
    adminUserId = Some(adminUser.id))

  private def getAdminUserRoles(user: User): Set[String] = {
    user.roles.map(r => r.toString())
  }

  override def alreadyBeenProcessed(requestId: String): Boolean = {
    accountCreditStore.getCreditBalanceByResquestID(requestId).nonEmpty
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

