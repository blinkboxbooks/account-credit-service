package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User
import com.blinkbox.books.credit.db._
import org.joda.time.DateTime
import scala.concurrent.Future
import com.blinkbox.books.time.SystemClock
import com.blinkbox.books.auth.UserRole
import scala.concurrent.ExecutionContext.Implicits.global

trait AdminService {
  def debit(i: Int, money: Money, s: String): Boolean
  def addCredit(req: Credit, customerId: Int)(implicit adminUser: User): Future[Unit]
  def lookupCreditHistoryForUser(userId: Int): Option[CreditHistory]
  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean
}

class DefaultAdminService(accountCreditStore: AccountCreditStore) extends AdminService {

  val nowTime = SystemClock.now()

  def debit(i: Int, money: Money, s: String): Boolean = money.value < lookupCreditBalanceForUser(i).value

  private def lookupCreditBalanceForUser(i: Int): Money = Money(BigDecimal.valueOf(1000000))

  def lookupCreditHistoryForUser(userId: Int): Option[CreditHistory] =
    if (userId == 7)
      Some(DefaultAdminService.dummy)
    else
      None

  def hasRequestAlreadyBeenProcessed(requestId: String): Boolean = {
    accountCreditStore.getCreditBalanceByRequestId(requestId).nonEmpty
  }

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

object DefaultAdminService {
  val dummy = {
    val thePast = new DateTime(2012,1,2,3,4,5)
    val cheap = Money(BigDecimal.valueOf(1000.53))
    val requestId = "sdfnaksfniofgniaodoir84t839t"
    val credits = List(Credit(requestId, thePast, cheap, CreditReason.CreditVoucherCode, CreditIssuer("James Bond", Set(UserRole.CustomerServicesRep))))
    val debits = List(Debit(requestId, thePast, cheap))
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val eithers = (credits ++ debits).sortBy {
      case Debit(rq, dt, _) => dt
      case Credit(rq, dt, _, _, _) => dt
    }
    CreditHistory(cheap, eithers)
  }
}