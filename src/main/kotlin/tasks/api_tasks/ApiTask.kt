package tasks.api_tasks

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import models.api_extracts.ApiResponses
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * Base class for API tasks.
 * This class provides common functionality for tasks that interact with APIs.
 *
 * @property taskName The name of the task.
 * @property taskSchedule The schedule for the task.
 * @property apiUrl The URL of the API to interact with.
 */
open class ApiTask(
    val taskName: String,
    val taskSchedule: String,
    val apiUrl: String,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val today: LocalDate = LocalDate.now()
    val yesterday: LocalDate = today.minusDays(1)
    val client =
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60000
                connectTimeoutMillis = 60000
                socketTimeoutMillis = 60000
            }
        }
    val defaultJson =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

    /**
     * Checks if an API response already exists for the given item.
     */
    fun checkExistingApiResponse(item: String): Boolean {
        val existingResponse =
            ApiResponses
                .select {
                    ApiResponses.apiResponseTaskKey eq
                        "$item-$today-$taskName"
                }.count()
        return existingResponse > 0
    }

    /**
     * Inserts an API response into the database.
     *
     * @param item The item for which the API response is being inserted.
     * @param httpResponse The HTTP response received from the API.
     */
    fun insertApiResponse(
        item: String,
        httpResponse: HttpResponse,
    ) {
        logger.info("Inserting API Response Data For $item-$today-$taskName")
        ApiResponses.insert {
            it[apiResponseKey] = "$item-$today"
            it[apiResponseTaskKey] = "$item-$today-$taskName"
            it[task] = taskName
            it[status] = httpResponse.status.toString()
            it[createdAt] = today
        }
    }
}
