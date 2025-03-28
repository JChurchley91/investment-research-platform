package tasks

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import models.ApiResponses
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

open class ApiTask(
    val taskName: String,
    val taskSchedule: String,
    val apiUrl: String,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val today: LocalDate = LocalDate.now()
    val yesterday: LocalDate = LocalDate.now().minusDays(1)
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

    fun checkExistingApiResponse(item: String): Boolean {
        val existingResponse =
            ApiResponses
                .select {
                    ApiResponses.apiResponseTaskKey eq
                        "$item-$today-$taskName"
                }.count()
        return existingResponse > 0
    }

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
            it[response] = httpResponse.toString()
            it[createdAt] = today
        }
    }
}
