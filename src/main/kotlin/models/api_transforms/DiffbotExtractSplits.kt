package models.api_transforms
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents a database table for storing cleansed Diffbot API extract data.
 * This table stores cleansed versions of diffbot text extracts.
 */
object DiffbotExtractSplits : IntIdTable("cleansed.diffbot_extract_split") {
    val diffbotExtractId = integer("diffbot_extract_sentence_id")
    val apiResponseKey = varchar("api_response_key", 255)
    val diffbotExtractSentence = text("cleansed_extracted_text")
    val createdAt = date("created_at")
}
