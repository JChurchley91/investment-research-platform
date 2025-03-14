package models

import org.jetbrains.exposed.dao.id.IntIdTable

const val MAX_VARCHAR_LENGTH = 255

object ApiResponse : IntIdTable("api_responses") {
    val status = varchar("status", MAX_VARCHAR_LENGTH)
    val response = varchar("response", MAX_VARCHAR_LENGTH)
    val createdAt = varchar("created_at", MAX_VARCHAR_LENGTH)
}