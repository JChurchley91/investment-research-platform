package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object DiffbotExtract : IntIdTable("raw.diffbot_extracts") {
    val apiResponseKey = varchar("api_response_key", 255)
    val task = varchar("task_name", 255)
    val html = text("html")
    val text = text("text")
    val createdAt = date("created_at")
}
