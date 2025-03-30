package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the daily coin prices table in the database.
 * This table stores the daily prices of various coins.
 */
object DailyCoinPrices : IntIdTable("raw.daily_coin_prices") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val open = double("open")
    val high = double("high")
    val low = double("low")
    val close = double("close")
    val volume = double("volume")
    val createdAt = date("created_at")
}