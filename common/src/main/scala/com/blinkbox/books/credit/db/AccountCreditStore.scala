package com.blinkbox.books.credit.db

import com.blinkbox.books.config.DatabaseConfig
import com.blinkbox.books.slick.{ DatabaseComponent, DatabaseSupport, MySQLDatabaseSupport, TablesContainer }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ ExecutionContext, Future }
import scala.slick.driver.MySQLDriver
import scala.slick.jdbc.JdbcBackend.Database

trait AccountCreditStore {
  def addCredit(credit: CreditBalance): Int
  def getCreditBalanceByRequestID(requestId: String): Option[CreditBalance]
  def getCreditBalanceById(creditBalanceId: Int): Option[CreditBalance]
  def getCreditHistoryForUser(userId: Int): Seq[CreditBalance]
}

class DbAccountCreditStore[DB <: DatabaseSupport](db: DB#Database, tables: AccountCreditTables[DB#Profile], exceptionFilter: DB#ExceptionFilter, implicit val exc: ExecutionContext) extends AccountCreditStore with StrictLogging {

  import tables._
  import driver.simple._

  override def getCreditHistoryForUser(userId: Int): Seq[CreditBalance] = {
    db.withSession { implicit session =>
      creditBalance.filter { _.customerId === userId }.list.toSeq
    }
  }

  override def addCredit(credit: CreditBalance): Int =
    db.withSession { implicit session =>
      (creditBalance returning creditBalance.map(_.id)) insert credit

    }

  override def getCreditBalanceByRequestID(requestId: String): Option[CreditBalance] =
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