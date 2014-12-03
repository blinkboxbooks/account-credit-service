package com.blinkbox.books.credit.admin

import com.blinkbox.books.config.AuthClientConfig
import com.typesafe.config.Config
import com.blinkbox.books.config.DatabaseConfig

case class AppConfig(auth: AuthClientConfig, interface: String, port: Int, databaseConfig : DatabaseConfig)

object AppConfig {
  def apply(c: Config): AppConfig = AppConfig(
    AuthClientConfig(c),
    c.getString("admin.listen.address"),
    c.getInt("admin.listen.port"),
    DatabaseConfig(c, "account.credit.db")
  )
}