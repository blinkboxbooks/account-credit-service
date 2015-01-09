package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.UserRole._
import org.joda.time.DateTime
import org.joda.money.{CurrencyUnit, Money}
import scala.util.Try

sealed trait CreditOrDebit

case class Amount( value: BigDecimal, currency: String = "GBP") {
  val jodaCurrency = Try(CurrencyUnit.of(currency)).filter(_.getCurrencyCode == "GBP").getOrElse(throw InvalidRequestException("unsupported_currency"))
   val asJodaMoney = Money.of(jodaCurrency, value.bigDecimal)
}

case class Debit(requestId: String, dateTime: DateTime, amount: Amount) extends CreditOrDebit 

case class Credit(requestId: String, dateTime: DateTime, amount: Amount, reason: String, issuer: CreditIssuer) extends CreditOrDebit 

case class CreditIssuer(name: String, roles: Set[UserRole])

case class CreditHistory(netBalance: Amount, history: List[CreditOrDebit])

object CreditHistory {
  def buildFromCreditBalances(cbs: Seq[CreditBalance]): CreditHistory = {
    val history = cbs.map { cb: CreditBalance =>
      if (cb.transactionType == TransactionType.Debit)
        Debit(cb.requestId, cb.createdAt, Amount(cb.value))
      else
        Credit(cb.requestId, cb.createdAt, Amount(cb.value), cb.reason.get.toString(), CreditIssuer(cb.adminUserId.get.toString, Set()))
    }

    val netBalance = history.foldLeft(BigDecimal(0))((cumulativeAmount, creditOrDebit) => creditOrDebit match {
      case Credit(_, _, amount, _, _) => cumulativeAmount + amount.value
      case Debit(_, _, amount) => cumulativeAmount - amount.value
    })

    CreditHistory(Amount(netBalance), history.toList)
  }
}