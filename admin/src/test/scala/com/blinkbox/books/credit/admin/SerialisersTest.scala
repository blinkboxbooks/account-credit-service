package com.blinkbox.books.credit.admin

import org.joda.time.DateTime
import org.scalatest.FlatSpec

class SerialisersTest extends FlatSpec {

  import com.blinkbox.books.credit.admin.Serialisers._
  import org.json4s.jackson.JsonMethods._

  "CreditHistory serialiser" should "serialise debits" in {
    val debit: Debit = Debit(new DateTime(2010, 1, 1, 1, 1, 1), Money(BigDecimal.valueOf(100)))
    assert(compact(render(Left(debit))) == """{"type":"debit","dateTime":"20100101T010101Z","amount":{"currency":"GBP","value":100.0}}""")
  }

  it should "serialise credits" in {
    val when = new DateTime(2010, 2, 3, 4, 5, 6)
    val amount = Money(BigDecimal.valueOf(100))
    val reason = CreditReason("why not")
    val issuer = CreditIssuer("James Bond", Set(Role("csr")))
    val credit = Credit(when, amount, reason, issuer)
    assert(compact(render(Right(credit))) == """{"type":"credit","dateTime":"20100203T040506Z","amount":{"currency":"GBP","value":100.0},"reason":"why not","issuerName":"James Bond"}""")
  }

}
