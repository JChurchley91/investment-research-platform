package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the daily news articles table in the database.
 * This table stores the daily news articles fetched from various sources.
 */
object DailyNewsArticles : IntIdTable("raw.daily_news_articles") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val title = varchar("title", 255)
    val url = varchar("url", 255)
    val sourceDomain = varchar("source_domain", 255)
    val overallSentimentLabel = varchar("overall_sentiment_label", 255)
    val createdAt = date("created_at")
}