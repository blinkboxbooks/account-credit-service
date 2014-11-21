package com.blinkbox.books.credit.db

import com.blinkbox.books.config.DatabaseConfig
import com.blinkbox.books.slick.{MySQLDatabaseSupport, DatabaseSupport}

import scala.slick.driver.{MySQLDriver, JdbcDriver}
import org.joda.time.{DateTimeZone, DateTime}
import java.sql.Timestamp
import scala.slick.jdbc.JdbcBackend.Database

trait DatabaseComponent extends com.blinkbox.books.slick.DatabaseComponent {
  type Tables = AccountCreditTables[DB.Profile]
}

trait RepositoriesComponent extends com.blinkbox.books.slick.RepositoriesComponent {
  self: DatabaseComponent =>
  val creditBalanceRepository: CreditBalanceRepository[DB.Profile]
}

trait DefaultRepositoriesComponent extends RepositoriesComponent {
  self: DatabaseComponent =>
  override val creditBalanceRepository = new DefaultCreditBalanceRepository(tables)
}

trait DefaultDatabaseComponent extends DatabaseComponent {
  val dbConf: DatabaseConfig
  override lazy val DB = new MySQLDatabaseSupport

  override lazy val driver = MySQLDriver

  override lazy val db = {
    Database.forURL(
      driver = "com.mysql.jdbc.Driver",
      url = dbConf.jdbcUrl,
      password = dbConf.pass,
      user = dbConf.user
    )
  }
  override val tables = AccountCreditTables[DB.Profile](driver)
}

/**
 * Database table.
 */
trait TableComponent {
  val driver: JdbcDriver
  def db: driver.backend.Database
}
