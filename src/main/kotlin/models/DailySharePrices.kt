package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object DailySharePrices : IntIdTable("raw.daily_share_prices") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val open = double("open")
    val high = double("high")
    val low = double("low")
    val close = double("close")
    val volume = double("volume")
    val createdAt = datetime("created_at")
}
