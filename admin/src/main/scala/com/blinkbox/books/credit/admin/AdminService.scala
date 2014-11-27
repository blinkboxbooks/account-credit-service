package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.auth.UserRole
import com.blinkbox.books.credit.db._
import com.blinkbox.books.time.Clock
import com.blinkbox.books.time.TimeSupport
import scala.concurrent.Future
import com.blinkbox.books.time.SystemClock
import com.blinkbox.books.auth.UserRole
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[CreditForRendering]
}

class DefaultAdminService(accountCreditStore: AccountCreditStore) extends AdminService {

  val nowTime = SystemClock.now()

  override def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[CreditForRendering] = {
    val foundCreditBalance = accountCreditStore.getCreditBalanceByResquestID(req.requestId)

    foundCreditBalance match {
      case Some(_) => Future { copyCreditBalanceToDTO(foundCreditBalance.get, adminUser) }
      case None    => Future { copyCreditBalanceToDTO(addCreditToDb(req, adminUser, customerId), adminUser) }
    }
  }

  private def addCreditToDb(req: Credit, adminUser: User, customerId: Int): CreditBalance = {
    val creditBalance = copyAddCreditReqToCreditBalance(req, customerId, adminUser)
    val id = accountCreditStore.addCredit(creditBalance)
    accountCreditStore.getCreditBalanceById(id).get
  }

  private def copyCreditBalanceToDTO(creditBalance: CreditBalance, adminUser: User): CreditForRendering = new CreditForRendering(
    creditBalance.requestId,
    creditBalance.createdAt,
    new Money(creditBalance.value, "GBP"),
    creditBalance.reason.get.toString(),
    Some(new CreditIssuerForRendering(adminUser.id.toString, getAdminUserRoles(adminUser))))

  private def copyAddCreditReqToCreditBalance(req: Credit, customerId: Int, adminUser: User): CreditBalance = CreditBalance(
    id = None,
    requestId = req.requestId,
    value = req.amount.amount,
    transactionType = TransactionType.Credit,
    reason = Some(com.blinkbox.books.credit.db.Reason.withName(req.reason.toString())),
    createdAt = nowTime,
    updatedAt = None,
    customerId = customerId,
    adminUserId = Some(adminUser.id))

  private def getAdminUserRoles(user: User): Set[String] = {
    user.roles.map(r => r.toString())
  }
}

