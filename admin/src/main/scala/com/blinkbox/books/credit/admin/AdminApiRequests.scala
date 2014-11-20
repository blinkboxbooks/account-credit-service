package com.blinkbox.books.credit.admin


import org.joda.time.DateTime

case class AddCreditRequest(
  requestId: String, 
  value: BigDecimal, 
  action: Int, 
  attributes: List[CampaignAttributeRequest], 
  startDate: DateTime, 
  endDate: Option[DateTime], 
  redemptionLimit: Option[Int], 
  initialVoucherCount: Option[Int]
) {
  AddCreditRequest.this match {
    case AddCreditRequest(_, _, _, _, startDate, Some(endDate), _, _) if endDate.isBefore(startDate) =>
      throw new ConstraintViolation("endDate", "endDate must not be before startDate")
    case _ =>
  }
}

