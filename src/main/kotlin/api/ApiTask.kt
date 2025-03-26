package api

import azure.SecretManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import models.ApiResponses
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

open class ApiTask(
    val taskName: String,
    val taskSchedule: String,
    val apiKeyName: String,
    val apiUrl: String,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val apiKey = SecretManager().getSecret(apiKeyName)
    val yesterday: LocalDate = LocalDate.now().minusDays(1)
    val client =
        HttpClient(CIO) {
        }
    val defaultJson =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

    fun checkExistingApiResponse(item: String): Boolean {
        val existingResponse =
            ApiResponses
                .select {
                    ApiResponses.apiResponseTaskKey eq
                        "$item-$yesterday-$taskName"
                }.count()
        return existingResponse > 0
    }

    fun insertApiResponse(
        item: String,
        httpResponse: HttpResponse,
    ) {
        logger.info("Inserting API Response Data For $item-$yesterday-$taskName")
        ApiResponses.insert {
            it[apiResponseKey] = "$item-$yesterday"
            it[apiResponseTaskKey] = "$item-$yesterday-$taskName"
            it[task] = taskName
            it[status] = httpResponse.status.toString()
            it[response] = httpResponse.toString()
            it[createdAt] = LocalDateTime.now()
        }
    }
}
