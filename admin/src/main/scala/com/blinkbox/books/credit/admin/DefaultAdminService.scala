package com.blinkbox.books.credit.admin

import java.io.Closeable

import com.blinkbox.books.auth.User
import com.blinkbox.books.credit.db._
import com.blinkbox.books.slick.{ UnknownDatabaseException, ConstraintException }
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.control.NonFatal
import scala.util.{ Try, Failure, Success }

trait DefaultAdminService extends AdminService {
  self: DatabaseComponent with RepositoriesComponent =>

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val logger = LoggerFactory.getLogger(getClass)

  val notFound: String => IllegalArgumentException = (theUnfound) => new IllegalArgumentException(s"$theUnfound was not found")

  def nowTimeProvider: DateTime = DateTime.now() // abstraction to provide the time right now to facilitate testing.

  def newSession(): DB.Session = db.createSession()

  private def findCreditBalanceByRequestId(req: AddCreditRequest): Option[CreditBalance] = {
    db.withSession(implicit s => creditBalanceRepository.getCreditBalanceByResquestID(req.requestId))
  }
  override def addCredit(req: AddCreditRequest, adminUser: User, customerId: Int): AddCreditResponse = {
    def addCredit(cr: CreditBalance) = db.withSession { implicit session =>
      creditBalanceRepository.addCredit(cr)
    }
    def copyFromReq(req: AddCreditRequest): CreditBalance = CreditBalance(
      id = None,
      requestId = req.requestId,
      value = req.value,
      transactionType = TransactionType.Credit,
      reason = Some(com.blinkbox.books.credit.db.Reason.withName(req.reason.toString())),
      createdAt = nowTimeProvider,
      updatedAt = None,
      customerId = customerId,
      adminUserId = Some(adminUser.id))

    val alreadyAppliedBalance = findCreditBalanceByRequestId(req)

    if (alreadyAppliedBalance == Some) {
      AddCreditResponse(req.requestId, Money(alreadyAppliedBalance.get.value))
    } else {
      Try(addCredit(copyFromReq(req))) match {
        case Success(creditBalance) => AddCreditResponse(req.requestId, Money(creditBalance.value))
        case Failure(e) => throw new Exception(e)
      }
    }
  }
}
