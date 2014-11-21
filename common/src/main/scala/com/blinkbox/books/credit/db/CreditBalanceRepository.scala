package com.blinkbox.books.credit.db

import com.blinkbox.books.slick.{ TablesSupport, SlickTypes }
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import scala.slick.profile.BasicProfile
import com.github.tototoshi.slick._
import com.blinkbox.books.credit.db.CreditBalance

trait CreditBalanceRepository[Profile <: BasicProfile] extends SlickTypes[Profile] {

  /**
   * add new credit.
   */
  def addCredit(credit: CreditBalance)(implicit session: Session): CreditBalance

  /**
   * find creditBalance by requestId
   */
  def getCreditBalanceByResquestID(requestId: String)(implicit session: Session): Option[CreditBalance]
}

trait JdbcCreditBalanceRepository[Profile <: JdbcProfile]
  extends CreditBalanceRepository[Profile] with TablesSupport[Profile, AccountCreditTables[Profile]] {
  import tables._
  import tables.timestampMapper
  import tables.driver.simple._

  /**
   * add new credit.
   */
  override def addCredit(credit: CreditBalance)(implicit session: Session): CreditBalance = {
    (creditBalance returning creditBalance) insert credit
  }

  /**
   * find creditBalance by requestId
   */
  override def getCreditBalanceByResquestID(requestId: String)(implicit session: Session): Option[CreditBalance] = {
    creditBalance.filter { _.requestId === requestId }.firstOption
  }
}

class DefaultCreditBalanceRepository[Profile <: JdbcProfile](val tables: AccountCreditTables[Profile])
  extends JdbcCreditBalanceRepository[Profile]
