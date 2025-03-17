package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ApiResponsesBody : IntIdTable("raw.api_response_bodies") {
    val response_body = varchar("response_body", 500)
    val createdAt = datetime("created_at")
}
