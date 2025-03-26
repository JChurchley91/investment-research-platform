package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object DailyNewsArticles : IntIdTable("raw.daily_news_articles") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val title = varchar("title", 255)
    val url = varchar("url", 255)
    val sourceDomain = varchar("source_domain", 255)
    val overallSentimentLabel = varchar("overall_sentiment_label", 255)
    val createdAt = datetime("created_at")
}
