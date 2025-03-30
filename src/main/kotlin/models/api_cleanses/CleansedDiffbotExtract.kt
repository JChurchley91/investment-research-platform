package models.api_cleanses
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents a database table for storing cleansed Diffbot API extract data.
 * This table stores cleansed versions of diffbot text extracts.
 */
object CleansedDiffbotExtract: IntIdTable("cleansed.diffbot_extracts") {
    val apiResponseKey = varchar("api_response_key", 255)
    val cleansedExtractedText = text("cleansed_extracted_text")
    val createdAt = date("created_at")
}