package models.api_extracts

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

/**
 * Represents the API responses table in the database.
 * This table stores the responses from various API calls made by the application.
 */
object ApiResponses : IntIdTable("raw.api_responses") {
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseTaskKey = varchar("api_response_task_key", 255)
    val task = varchar("task_name", 255)
    val status = varchar("status", 255)
    val createdAt = date("created_at")
}
