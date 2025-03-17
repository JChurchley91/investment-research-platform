package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

const val MAX_VARCHAR_LENGTH = 255

object ApiResponse : IntIdTable("raw.api_responses") {
    val status = varchar("status", MAX_VARCHAR_LENGTH)
    val response = varchar("response", MAX_VARCHAR_LENGTH)
    val createdAt = datetime("created_at")
}
