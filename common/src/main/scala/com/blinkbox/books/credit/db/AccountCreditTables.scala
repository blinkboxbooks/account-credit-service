package com.blinkbox.books.credit.db

import java.sql.Timestamp
import com.blinkbox.books.slick.TablesContainer
import org.joda.time.{ DateTimeZone, DateTime }
import scala.slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf
import Reason._
import TransactionType._

trait AccountCreditTables[Profile <: JdbcProfile] extends TablesContainer[Profile] {
  import driver.simple._

  // TODO - how to make these generic? Note that the driver instance *has* to be implicitly available for this to work
  implicit lazy val reasonMapper = MappedColumnType.base[Reason.Reason, Int](_.id, Reason.apply)
  implicit lazy val transactionTypeMapper = MappedColumnType.base[TransactionType.TransactionType, Int](_.id, TransactionType.apply)
  implicit lazy val timestampMapper = MappedColumnType.base[DateTime, Timestamp](
    dateTime => new Timestamp(dateTime.getMillis),
    timestamp => new DateTime(timestamp).toDateTime(DateTimeZone.UTC))

  val creditBalance = TableQuery[CreditBalanceTable]

  class CreditBalanceTable(tag: Tag) extends Table[CreditBalance](tag, "credit_balance") {
    def id = column[Int]("credit_balance_id", O.PrimaryKey, O.AutoInc)
    def requestId = column[String]("request_id", O.NotNull)
    def value = column[BigDecimal]("value", O.DBType("decimal(7, 2)"), O.NotNull)
    def transactionType = column[TransactionType]("transaction_types_id", O.NotNull)
    def reason = column[Reason]("reasons_id ", O.Nullable)
    def createdAt = column[DateTime]("created_at", O.NotNull)
    def updatedAt = column[DateTime]("updated_at", O.Nullable)
    def customerId = column[Int]("customer_id", O.NotNull)
    def adminUserId = column[Int]("admin_user_id", O.Nullable)
    def * = (id.?, requestId, value, transactionType, reason.?, createdAt, updatedAt.?, customerId, adminUserId.?) <> (CreditBalance.tupled, CreditBalance.unapply _)
  }
}

object AccountCreditTables {
  def apply[Profile <: JdbcProfile](_driver: Profile) = new AccountCreditTables[Profile] {
    override val driver = _driver
  }
}
