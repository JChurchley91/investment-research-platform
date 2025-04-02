package models.api_enhancements
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents a database table for storing cleansed Diffbot API extract data.
 * This table stores cleansed versions of diffbot text extracts.
 */
object DiffbotExtractSplits : IntIdTable("enhanced.diffbot_extract_splits") {
    val diffbotExtractId = varchar("diffbot_extract_sentence_id", 255)
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseArticleKey = varchar("api_response_article_key", 255)
    val diffbotExtractSentence = text("diffbot_extract_sentence_text")
    val createdAt = date("created_at")
}
