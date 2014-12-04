package com.blinkbox.books.credit.admin

import com.blinkbox.books.credit.db.{TransactionType, CreditBalance, AccountCreditStore}
import com.blinkbox.books.test.MockitoSyrup
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.mockito.Mockito._


class AdminServiceTest extends FlatSpec with BeforeAndAfter with MockitoSyrup with ScalaFutures {

  var accountCreditStore: AccountCreditStore = _
  var adminService: AdminService = _

  before {
    accountCreditStore = mock[AccountCreditStore]
    adminService = new DefaultAdminService(accountCreditStore)
  }

  "getCreditHistoryForUser" should "correctly compute the net balance" in {
    val creditHistory = List(
      CreditBalance(Some(1), "foo", 13.37, TransactionType.Credit, None, new DateTime(), None, 1, Some(666)),
      CreditBalance(Some(2), "bar", 5.01, TransactionType.Debit, Some(com.blinkbox.books.credit.db.Reason.CreditRefund), new DateTime(), None, 1, None))
    when(accountCreditStore.getCreditHistoryForUser(1)).thenReturn(creditHistory)

    whenReady(adminService.lookupCreditHistoryForUser(1)) { result =>
      assert(result.netBalance.amount == 8.36)
      assert(result.history.size == 2)
    }
  }
}