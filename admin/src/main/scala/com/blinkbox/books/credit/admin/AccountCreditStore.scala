package com.blinkbox.books.credit.admin

import com.blinkbox.books.config.DatabaseConfig
import com.blinkbox.books.slick.{DatabaseComponent, DatabaseSupport, MySQLDatabaseSupport, TablesContainer}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.slick.driver.MySQLDriver
import scala.slick.jdbc.JdbcBackend.Database
import CreditHistory._

trait AccountCreditStore {
  def addCredit(credit: CreditBalance): Future[Int]
  def addDebitIfUserHasSufficientCredit(userId: Int, requestId: String, amount: Amount): Future[Unit]
  def getCreditBalanceByRequestId(requestId: String): Future[Option[CreditBalance]]
  def getCreditBalanceById(creditBalanceId: Int): Future[Option[CreditBalance]]
  def getCreditHistoryForUser(userId: Int): Future[Seq[CreditBalance]]
}

class DbAccountCreditStore[DB <: DatabaseSupport](db: DB#Database, tables: AccountCreditTables[DB#Profile], exceptionFilter: DB#ExceptionFilter, implicit val exc: ExecutionContext) extends AccountCreditStore with StrictLogging {

  import tables._
  import driver.simple._

  override def getCreditHistoryForUser(userId: Int): Future[Seq[CreditBalance]] = Future {
    db.withSession { implicit session =>
      creditBalance.filter(_.customerId === userId).sortBy(_.createdAt.desc).list.toSeq
    }
  } transform(identity, exceptionFilter.default)

  override def addCredit(credit: CreditBalance): Future[Int] = Future {
    db.withSession { implicit session =>
      (creditBalance returning creditBalance.map(_.id)) insert credit
    }
  } transform(identity, exceptionFilter.default)

  override def addDebitIfUserHasSufficientCredit(userId: Int, requestId: String, amount: Amount): Future[Unit] = Future {
    db.withSession { implicit session =>
      val creditHistory = buildFromCreditBalances(creditBalance.filter(_.customerId === userId).list.toSeq)
      val newBalance = creditHistory.netBalance.value - amount.value
      val insufficientFunds = newBalance < 0
      if (insufficientFunds)
        throw new InsufficientFundsException
      else
        addDebit(CreditBalanceFactory.fromDebit(requestId, amount.value, userId))
    }
  } transform(_ => (), exceptionFilter.default)

  private def addDebit(credit: CreditBalance): Future[Int] = addCredit(credit)
  
  override def getCreditBalanceByRequestId(transactionId: String): Future[Option[CreditBalance]] = Future {
    db.withSession { implicit session =>
      creditBalance.filter(_.transactionId === transactionId).firstOption
    }
  } transform(identity, exceptionFilter.default)

  override def getCreditBalanceById(creditBalanceId: Int): Future[Option[CreditBalance]] = Future {
    db.withSession { implicit session =>
      creditBalance.filter(_.id === creditBalanceId).firstOption
    }
  } transform(identity, exceptionFilter.default)
}

class DefaultDatabaseComponent(config: DatabaseConfig) extends DatabaseComponent {

  override type Tables = TablesContainer[DB.Profile]

  override val driver = MySQLDriver
  override val DB = new MySQLDatabaseSupport
  override val db = Database.forURL(
    driver = "com.mysql.jdbc.Driver",
    url = config.jdbcUrl,
    user = config.user,
    password = config.pass)
  override val tables = AccountCreditTables[DB.Profile](driver)
}
