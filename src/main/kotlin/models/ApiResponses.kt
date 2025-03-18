package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ApiResponses : IntIdTable("raw.api_responses") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val status = varchar("status", 255)
    val response = varchar("response", 255)
    val createdAt = datetime("created_at")
}
