package models

import org.jetbrains.exposed.sql.Table

object ApiResponse : Table("api_responses") {
    val id = integer("id").autoIncrement()
    val status = varchar("status", 255)
    val response = text("response")
}