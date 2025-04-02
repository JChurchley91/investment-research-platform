package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the Diffbot extracts table in the database.
 * This table stores the extracts fetched from Diffbot API.
 */
object DiffbotExtract : IntIdTable("raw.diffbot_extracts") {
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseArticleKey = varchar("api_response_article_key", 255)
    val task = varchar("task_name", 255)
    val summary = text("summary")
    val createdAt = date("created_at")
}
