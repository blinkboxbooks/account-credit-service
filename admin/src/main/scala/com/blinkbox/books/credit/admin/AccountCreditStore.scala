package com.blinkbox.books.credit.admin

import com.blinkbox.books.config.DatabaseConfig
import com.blinkbox.books.slick.{DatabaseComponent, DatabaseSupport, MySQLDatabaseSupport, TablesContainer}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.slick.driver.MySQLDriver
import scala.slick.jdbc.JdbcBackend.Database

trait AccountCreditStore {
  def addCredit(credit: CreditBalance): Int
  def addDebitIfUserHasSufficientCredit(userId: Int, requestId: String, amount: Money): Unit
  def getCreditBalanceByRequestId(requestId: String): Option[CreditBalance]
  def getCreditBalanceById(creditBalanceId: Int): Option[CreditBalance]
  def getCreditHistoryForUser(userId: Int): Seq[CreditBalance]
}

class DbAccountCreditStore[DB <: DatabaseSupport](db: DB#Database, tables: AccountCreditTables[DB#Profile], exceptionFilter: DB#ExceptionFilter, implicit val exc: ExecutionContext) extends AccountCreditStore with StrictLogging {

  import tables._
  import driver.simple._

  override def getCreditHistoryForUser(userId: Int): Seq[CreditBalance] =
    db.withSession { implicit session =>
      creditBalance.filter { _.customerId === userId }.list.toSeq
    }

  override def addCredit(credit: CreditBalance): Int =
    db.withSession { implicit session =>
      (creditBalance returning creditBalance.map(_.id)) insert credit
    }

  override def addDebitIfUserHasSufficientCredit(userId: Int, requestId: String, amount: Money): Unit =
    db.withSession { implicit session =>
      val creditHistory = CreditHistory.buildFromCreditBalances(creditBalance.filter { _.customerId === userId }.list.toSeq)
      val newBalance = creditHistory.netBalance.value - amount.value
      val insufficientFunds = newBalance < 0
      if (insufficientFunds)
        throw new InsufficientFundsException
      else
        addDebit(CreditBalanceFactory.fromDebit(requestId, amount.value, userId))
    }

  private def addDebit(credit: CreditBalance): Int = addCredit(credit)
  
  override def getCreditBalanceByRequestId(requestId: String): Option[CreditBalance] =
    db.withSession {
      implicit session =>
        creditBalance.filter { _.requestId === requestId }.firstOption
    }

  override def getCreditBalanceById(creditBalanceId: Int): Option[CreditBalance] =
    db.withSession { implicit session =>
      creditBalance.filter { _.id === creditBalanceId }.firstOption
    }
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
