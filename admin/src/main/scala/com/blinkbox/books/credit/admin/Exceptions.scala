package com.blinkbox.books.gifting.db

case class NotFoundException(field: String) extends Exception(s"the requested $field was not found")
