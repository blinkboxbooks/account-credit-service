package com.blinkbox.books.credit.db

import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.BigDecimal.double2bigDecimal
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.Database

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Finders
import org.scalatest.FunSuite

import com.blinkbox.books.slick.DatabaseComponent
import com.blinkbox.books.slick.H2DatabaseSupport
import com.blinkbox.books.slick.TablesContainer
import com.blinkbox.books.time.SystemClock

trait TestDatabase extends DatabaseComponent {
  override val DB = new H2DatabaseSupport
  override type Tables = TablesContainer[DB.Profile]

  override def db = {
    val threadId = Thread.currentThread.getId
    Database.forURL(s"jdbc:h2:mem:library$threadId;DB_CLOSE_DELAY=-1;MODE=MYSQL;DATABASE_TO_UPPER=FALSE", driver = "org.h2.Driver")
  }

  override val driver = H2Driver
  override val tables = AccountCreditTables[DB.Profile](driver)
}

@RunWith(classOf[JUnitRunner])
class AccountCreditStoreTest extends FunSuite with BeforeAndAfterEach with TestDatabase {
  import tables.driver.simple._
  import tables._
  val dao = new DbAccountCreditStore[H2DatabaseSupport](db, tables, exceptionFilter, global)
  val nowTime = SystemClock.now()

  override def beforeEach() {
    db.withSession { implicit session =>
      creditBalance.ddl.create
    }
  }

  override def afterEach() {
    db.withSession { implicit session =>
      creditBalance.ddl.drop
    }
  }

  test("add credit") {
    db.withSession { implicit session =>
      val creditBalance = newCredit(1)
      dao.addCredit(creditBalance)
      assert(dao.getCreditBalanceById(1).map(_.id) == Some(creditBalance.id))
      assert(dao.getCreditBalanceById(1).map(_.requestId) == Some(creditBalance.requestId))
    }
  }

  private def newCredit(id: Int): CreditBalance = new CreditBalance(
    Some(id),
    "377d0f7a57a9c41edsgdsfgf290f169254ed2f6d4",
    10.44,
    TransactionType.Credit,
    Some(Reason.GoodwillBookIssue),
    nowTime,
    None,
    2387,
    Some(889))
}    
  
