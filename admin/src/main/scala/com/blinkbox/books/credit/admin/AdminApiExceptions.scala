package com.blinkbox.books.credit.admin

case class InvalidRequestException(message: String) extends Exception(message)
class InsufficientFundsException extends Exception