package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.credit.db._
import com.blinkbox.books.time.Clock
import com.blinkbox.books.time.TimeSupport
import scala.concurrent.Future

trait AdminService {
  def addCredit(req: AddCreditRequest, customerId: Int)(implicit adminUser: User): CreditBalance
}

class DefaultAdminService(accountCreditStore: AccountCreditStore) extends AdminService with TimeSupport {

  override def addCredit(req: AddCreditRequest, adminUser: User, customerId: Int): CreditBalance = {

    val foundCreditBalance = findCreditBalanceByRequestId(req.requestId)
    foundCreditBalance match {
      case Some(_) => foundCreditBalance.get
      case None    => addCreditToDb(req, adminUser, customerId)
    }
  }

  private def findCreditBalanceByRequestId(requestId: String): Option[CreditBalance] = {
    accountCreditStore.getCreditBalanceByResquestID(requestId)
  }

  private def addCreditToDb(req: AddCreditRequest, adminUser: User, customerId: Int): CreditBalance = {
    val creditBalance = CreditBalance(
      id = None,
      requestId = req.requestId,
      value = req.value,
      transactionType = new TransactionType (TransactionType.Credit),
      reason = Some(com.blinkbox.books.credit.db.Reason.withName(req.reason.toString())),
      createdAt = clock.now(),
      updatedAt = None,
      customerId = customerId,
      adminUserId = Some(adminUser.id))

    accountCreditStore.addCredit(creditBalance)
  }
}
