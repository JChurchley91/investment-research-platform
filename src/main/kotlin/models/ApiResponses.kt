package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object ApiResponses : IntIdTable("raw.api_responses") {
    val apiResponseKey = varchar("api_response_key", 255)
    val apiResponseTaskKey = varchar("api_response_task_key", 255)
    val task = varchar("task_name", 255)
    val status = varchar("status", 255)
    val response = varchar("response", 255)
    val createdAt = date("created_at")
}
