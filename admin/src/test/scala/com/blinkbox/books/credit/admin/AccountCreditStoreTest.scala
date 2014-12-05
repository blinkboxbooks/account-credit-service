package com.blinkbox.books.credit.admin

import com.blinkbox.books.slick.{DatabaseComponent, H2DatabaseSupport, TablesContainer}
import com.blinkbox.books.time.SystemClock
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.math.BigDecimal.double2bigDecimal
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.Database

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
  def nowTime = SystemClock.now()

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

  test("get credit history") {
    val credit = new CreditBalance(Some(1), "foo", 10.44, TransactionType.Credit, Some(Reason.GoodwillBookIssue), nowTime, None, 2387, Some(889))
    db.withSession { implicit session =>
      dao.addCredit(credit)
    }

    db.withSession { implicit session =>
      assert(dao.getCreditHistoryForUser(2387) == List(credit))
    }
  }

  test("add debit") {
    val credit = 10
    db.withSession { implicit session =>
      dao.addCredit(CreditBalanceFactory.fromCredit("foo", credit, Reason.GoodwillBookIssue, 2387, 889))
    }

    val balance = credit

    db.withSession { implicit session =>
      dao.addDebit(2387, "foo", Money(BigDecimal(credit - 1)))
    }

    db.withSession { implicit session =>
      val history = CreditHistory.buildFromCreditBalances(dao.getCreditHistoryForUser(2387))
      assert(history.netBalance.value == 1)
      assert(history.history.size == 2)
    }
  }

  test("adding debit without having sufficient credit should throw exception") {
    val credit = 10
    db.withSession { implicit session =>
      dao.addCredit(CreditBalanceFactory.fromCredit("foo", credit, Reason.GoodwillBookIssue, 2387, 889))
    }

    db.withSession { implicit session =>
      val history = CreditHistory.buildFromCreditBalances(dao.getCreditHistoryForUser(2387))
      assert(history.netBalance.value == credit)
      assert(history.history.size == 1)
    }

    val balance = credit

    db.withSession { implicit session =>
      try {
        dao.addDebit(2387, "foo", Money(BigDecimal(balance + 1)))
        fail("Expected to throw exception, but didn't.")
      } catch {
        case ex: InsufficientFundsException =>
        case ex: Exception => fail("Unexpected exception: $ex")
      }
    }

    db.withSession { implicit session =>
      val history = CreditHistory.buildFromCreditBalances(dao.getCreditHistoryForUser(2387))
      assert(history.netBalance.value == credit)
      assert(history.history.size == 1)
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
  
