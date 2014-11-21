package com.blinkbox.books.credit.admin

import com.blinkbox.books.auth.User

trait AdminService {
  
 def addCredit(req: AddCreditRequest, adminUser : User ,customerId: Int): AddCreditResponse
}