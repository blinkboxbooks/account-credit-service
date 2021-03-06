package com.blinkbox.books.credit.admin

import com.blinkbox.books.slick.{DatabaseComponent, H2DatabaseSupport, TablesContainer}
import com.blinkbox.books.time.SystemClock
import com.google.common.util.concurrent.MoreExecutors
import org.junit.runner.RunWith
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.double2bigDecimal
import scala.slick.driver.H2Driver
import scala.slick.jdbc.JdbcBackend.Database
import scala.util.Random

import CreditHistory.buildFromCreditBalances

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
class AccountCreditStoreTest extends FunSuite with BeforeAndAfterEach with TestDatabase with ScalaFutures with AsyncAssertions {
  import tables.driver.simple._
  import tables._

  implicit val directExecutor = ExecutionContext.fromExecutor(MoreExecutors.directExecutor())
  val dao = new DbAccountCreditStore[H2DatabaseSupport](db, tables, exceptionFilter, directExecutor)
  def nowTime = SystemClock.now()
  val log = LoggerFactory.getLogger(getClass)
  val customerId = 2387
  val adminId = 889

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
      whenReady(dao.getCreditBalanceById(1)) { actual =>
        assert(actual.fold(false)(_ == creditBalance))
      }
    }
  }

  test("get credit history") {
    val credit = new CreditBalance(Some(1), "foo", 10.44, TransactionType.Credit, Some(Reason.GoodwillBookIssue), nowTime, None, customerId, Some(adminId))
    db.withSession { implicit session =>
      dao.addCredit(credit)
    }

    db.withSession { implicit session =>
      whenReady(dao.getCreditHistoryForUser(customerId)) { actual =>
        assert(actual == List(credit))
      }
    }
  }

  test("add debit") {
    val credit = 10
    db.withSession { implicit session =>
      dao.addCredit(CreditBalanceFactory.fromCredit("foo", credit, Reason.GoodwillBookIssue, customerId, adminId))
    }

    val balance = credit

    db.withSession { implicit session =>
      dao.addDebitIfUserHasSufficientCredit(customerId, "foo", Amount(BigDecimal(credit - 1)))
    }

    db.withSession { implicit session =>
      val actual = dao.getCreditHistoryForUser(customerId).map(buildFromCreditBalances)
      assertCreditHistory(actual, expectedValue = BigDecimal(1), expectedSize = 2)
    }
  }

  test("adding debit without having sufficient credit should throw exception") {
    val credit = 10
    db.withSession { implicit session =>
      dao.addCredit(CreditBalanceFactory.fromCredit("foo", credit, Reason.GoodwillBookIssue, customerId, adminId))
    }

    db.withSession { implicit session =>
      val actual = dao.getCreditHistoryForUser(customerId).map(buildFromCreditBalances)
      assertCreditHistory(actual, expectedValue = credit, expectedSize = 1)
    }

    val balance = credit

    db.withSession { implicit session =>
      val fut = dao.addDebitIfUserHasSufficientCredit(customerId, "foo", Amount(BigDecimal(balance + 1)))
      assert(fut.eitherValue.fold(false){
        case Right(_) => false
        case Left(e) => e.isInstanceOf[InsufficientFundsException]
      }, "Expected InsufficientFundsException to be thrown")
    }

    db.withSession { implicit session =>
      val history = dao.getCreditHistoryForUser(customerId).map(buildFromCreditBalances)
      assertCreditHistory(history, expectedValue = credit, expectedSize = 1)
    }
  }

  test("credit history is sorted by descending createdAt time") {
    val credits = for (i <- 1 to 100) yield CreditBalance(None, s"request-$i", BigDecimal(1), TransactionType.Credit,
        Some(Reason.GoodwillBookIssue), nowTime.plusDays(i), None, customerId, Some(adminId))
    Random.shuffle(credits).foreach(dao.addCredit)
    whenReady(dao.getCreditHistoryForUser(customerId)) { balances =>
      balances.zip(balances.drop(1)).foreach {
        case (first, second) => assert(first.createdAt.isAfter(second.createdAt))
      }
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
    customerId,
    Some(adminId))

  private def assertCreditHistory(history: Future[CreditHistory], expectedValue: BigDecimal, expectedSize: Int) = {
    whenReady(history) { h =>
      assert(h.netBalance.value == expectedValue)
      assert(h.history.size == expectedSize)
    }
  }
}    
  
