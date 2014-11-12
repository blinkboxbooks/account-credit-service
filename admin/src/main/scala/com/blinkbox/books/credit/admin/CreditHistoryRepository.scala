package com.blinkbox.books.credit.admin

import org.joda.time.DateTime

class CreditHistoryRepository {
  def lookupCreditHistoryForUser(userId: Int): CreditHistory = {
    val thePast = new DateTime().minusDays(5)
    val cheap = Money(BigDecimal.valueOf(1000))

    val credits: List[Credit] = List(Credit(thePast, cheap, CreditReason("Why not?"), CreditIssuer("James Bond", Set(Role("csr")))))
    val debits: List[Debit] = List(Debit(thePast, cheap))
    val creditsAsEither: List[Either[Debit, Credit]] = credits.map(f => Right(f))
    val debitsAsEither: List[Either[Debit, Credit]] = debits.map(f => Left(f))
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val eithers = (creditsAsEither ++ debitsAsEither).sortBy {
      case Left(Debit(dt, _)) => dt
      case Right(Credit(dt, _, _, _)) => dt
    }
    CreditHistory(cheap, eithers)
  }

}
