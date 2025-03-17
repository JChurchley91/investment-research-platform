package api

import gcp.DatabaseFactory
import gcp.SecretManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import models.ApiResponse
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class TrendingNews {
    private val logger = LoggerFactory.getLogger(TrendingNews::class.java)
    val taskName: String = "TrendingNews"
    val taskSchedule: String = "* * * * *"
    val apiKey = SecretManager().getSecret("newsdata-api-key")
    val cryptoCoins = listOf("BTC", "ETC", "ADA", "XRP")
    val apiUrl: String = "https://newsdata.io/api/1/news?"

    fun initialize() {
        try {
            DatabaseFactory.init()
            transaction {
                exec("CREATE SCHEMA IF NOT EXISTS raw")
                SchemaUtils.create(ApiResponse)
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

                println(httpResponseBody)

                transaction {
                    exec("CREATE SCHEMA IF NOT EXISTS raw")
                    SchemaUtils.create(ApiResponse)
                    ApiResponse.insert {
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
