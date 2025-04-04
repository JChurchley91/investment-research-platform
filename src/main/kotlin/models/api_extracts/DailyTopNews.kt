package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the Diffbot extracts table in the database.
 * This table stores the extracts fetched from Diffbot API.
 */
object DailyTopNews : IntIdTable("raw.daily_top_news") {
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseArticleKey = varchar("api_response_article_key", 255)
    val url = varchar("url", 255)
    val title = varchar("title", 255)
    val createdAt = date("created_at")
}
