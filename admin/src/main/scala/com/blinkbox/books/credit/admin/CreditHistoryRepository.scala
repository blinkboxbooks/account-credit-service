package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.UserRole
import org.joda.time.DateTime

class CreditHistoryRepository {
  def lookupCreditHistoryForUser(userId: Int): Option[CreditHistory] =
    if (userId == 7)
      Some(CreditHistoryRepository.dummy)
    else
      None
}

object CreditHistoryRepository {
  val dummy = {
    val thePast = new DateTime(2012,1,2,3,4,5)
    val cheap = Money(BigDecimal.valueOf(1000.53))
    val requestId= "sdfnaksfniofgniaodoir84t839t"
    val credits: List[Credit] = List(Credit(requestId,thePast, cheap, CreditReason.CreditVoucherCode, CreditIssuer("James Bond", Set(UserRole.CustomerServicesRep))))
    val debits: List[Debit] = List(Debit(requestId,thePast, cheap))
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val eithers = (credits ++ debits).sortBy {
      case Debit(rq,dt, _) => dt
      case Credit(rq,dt, _, _, _) => dt
    }
    CreditHistory(cheap, eithers)
  }
}