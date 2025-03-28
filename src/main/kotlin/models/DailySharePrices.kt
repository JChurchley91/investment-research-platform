package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the daily share prices table in the database.
 * This table stores the daily share prices fetched from various sources.
 */
object DailySharePrices : IntIdTable("raw.daily_share_prices") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val open = double("open")
    val high = double("high")
    val low = double("low")
    val close = double("close")
    val volume = double("volume")
    val createdAt = date("created_at")
}
