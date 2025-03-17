package api

import gcp.DatabaseFactory
import io.ktor.client.*
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
import gcp.SecretManager

class TrendingNews {
    private val logger = LoggerFactory.getLogger(TrendingNews::class.java)
    val taskName: String = "TrendingNews"
    val taskSchedule: String = "* * * * *"
    val googleSearchKey: String = SecretManager().getSecret("google-search")

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
            val httpResponse: HttpResponse = client.get("https://ktor.io/")
            val httpResponseStatus: HttpStatusCode = httpResponse.status
            client.close()

            transaction {
                exec("CREATE SCHEMA IF NOT EXISTS raw")
                SchemaUtils.create(ApiResponse)
                ApiResponse.insert {
                    it[status] = httpResponseStatus.toString()
                    it[response] = httpResponse.toString()
                    it[createdAt] = LocalDateTime.now()
                }
            }
        } catch (exception: Exception) {
            logger.error("Error calling Bing News API: $exception")
        }
    }
}
