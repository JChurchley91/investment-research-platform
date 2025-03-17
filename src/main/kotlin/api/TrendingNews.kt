package api

import gcp.DatabaseFactory
import gcp.SecretManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import models.ApiResponses
import models.ApiResponsesBody
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalDate

class TrendingNews {
    val taskName: String = "TrendingNews"
    val taskSchedule: String = "* * * * *"
    val today: LocalDate = LocalDate.now()
    private val logger = LoggerFactory.getLogger(TrendingNews::class.java)
    private val apiKey = SecretManager().getSecret("newsdata-api-key")
    private val cryptoCoins = listOf("BTC", "ETC", "ADA", "XRP")
    private val apiUrl: String = "https://newsdata.io/api/1/news?"

    fun initialize() {
        try {
            DatabaseFactory.init()
            transaction {
                exec("CREATE SCHEMA IF NOT EXISTS raw")
                SchemaUtils.create(ApiResponses)
                SchemaUtils.create(ApiResponsesBody)
            }
        } catch (exception: Exception) {
            logger.error("Error initializing database: $exception")
        }
    }

    suspend fun callApi() {
        try {
            DatabaseFactory.init()
            val client = HttpClient(CIO)

            for (coin in cryptoCoins) {
                val httpResponse: HttpResponse =
                    client.get(
                        "${apiUrl}apikey=$apiKey&q=$coin&country=us&language=en&category=technology",
                    )
                val httpResponseStatus: HttpStatusCode = httpResponse.status
                val httpResponseBody: String = httpResponse.body()

                transaction {
                    ApiResponses.insert {
                        it[apiResponseKey] = "$coin-$today"
                        it[status] = httpResponseStatus.toString()
                        it[response] = httpResponse.toString()
                        it[createdAt] = LocalDateTime.now()
                    }
                }
            }
        } catch (exception: Exception) {
            logger.error("Error calling API: $exception")
        }
    }
}
