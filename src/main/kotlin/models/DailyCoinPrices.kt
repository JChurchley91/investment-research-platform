package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object DailyCoinPrices : IntIdTable("raw.daily_coin_prices") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val dailyCoinPrice = double("daily_coin_price")
    val percentageChange24H = double("percentage_change_24h")
    val createdAt = datetime("created_at")
}
