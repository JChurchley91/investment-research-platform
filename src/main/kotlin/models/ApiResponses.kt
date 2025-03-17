package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

const val MAX_VARCHAR_LENGTH = 255

object ApiResponses : IntIdTable("raw.api_responses") {
    val apiResponseKey = varchar("api_response_key", MAX_VARCHAR_LENGTH)
    val status = varchar("status", MAX_VARCHAR_LENGTH)
    val response = varchar("response", MAX_VARCHAR_LENGTH)
    val createdAt = datetime("created_at")
}
