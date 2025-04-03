package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object DailyNewsScreenshots : IntIdTable("raw.daily_news_screenshots") {
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseArticleKey = varchar("api_response_article_key", 255)
    val imageData = binary("image_data",
        length = Int.MAX_VALUE)
    val createdAt = date("created_at")
}
