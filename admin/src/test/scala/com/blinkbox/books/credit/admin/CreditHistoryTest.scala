package com.blinkbox.books.credit.admin

import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, FunSuite}

class CreditHistoryTest extends FlatSpec {

  "buildFromCreditBalances" should "correctly compute the net balance" in {
    val creditBalances = List(
      CreditBalance(Some(1), "foo", 13.37, TransactionType.Credit, None, new DateTime(), None, 1, Some(666)),
      CreditBalance(Some(2), "bar", 5.01, TransactionType.Debit, Some(Reason.CreditRefund), new DateTime(), None, 1, None))

    val creditHistory = CreditHistory.buildFromCreditBalances(creditBalances)
    assert(creditHistory.netBalance.value == 8.36)
    assert(creditHistory.history.size == 2)
  }

}
