package com.blinkbox.books.credit.admin

import com.blinkbox.books.credit.admin.Reason._
import com.blinkbox.books.credit.admin.TransactionType._
import com.blinkbox.books.slick.TablesContainer
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.ProvenShape.proveShapeOf

trait AccountCreditTables[Profile <: JdbcProfile] extends TablesContainer[Profile] {
  import driver.simple._

  implicit lazy val reasonMapper = MappedColumnType.base[Reason.Reason, Int](_.id, Reason.apply)
  implicit lazy val transactionTypeMapper = MappedColumnType.base[TransactionType.TransactionType, Int](_.id, TransactionType.apply)
  
  val creditBalance = TableQuery[CreditBalanceTable]

  class CreditBalanceTable(tag: Tag) extends Table[CreditBalance](tag, "credit_balance") {
    def id = column[Int]("credit_balance_id", O.PrimaryKey, O.AutoInc)
    def transactionId = column[String]("transaction_id")
    def value = column[BigDecimal]("value", O.DBType("decimal(7, 2)"))
    def transactionType = column[TransactionType]("transaction_type_id")
    def reason = column[Option[Reason]]("reason_id")
    def createdAt = column[DateTime]("created_at")
    def updatedAt = column[Option[DateTime]]("updated_at")
    def customerId = column[Int]("customer_id", O.NotNull)
    def adminUserId = column[Option[Int]]("admin_user_id")
    def * = (id.?, transactionId, value, transactionType, reason, createdAt, updatedAt, customerId, adminUserId) <> (CreditBalance.tupled, CreditBalance.unapply _)
  }
}

object AccountCreditTables {
  def apply[Profile <: JdbcProfile](_driver: Profile) = new AccountCreditTables[Profile] {
    override val driver = _driver
  }
}
