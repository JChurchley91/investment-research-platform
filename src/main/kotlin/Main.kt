import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.LoggerFactory
import azure.KeyVaultClient
import azure.DatabaseFactory
import io.ktor.http.HttpStatusCode
import models.ApiResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import java.time.LocalDateTime


suspend fun main() {
    val logger: org.slf4j.Logger = LoggerFactory.getLogger("news-fetcher")
    logger.info("Initializing database connection")
    DatabaseFactory.init()
    logger.info("Retrieving secret from Azure Key Vault")
    val testSecret: String = KeyVaultClient.getSecret("test-secret")
    logger.info("Sending request to ktor.io")
    val client = HttpClient(CIO)
    var httpResponse: HttpResponse = client.get("https://ktor.io/")
    var httpResponseStatus: HttpStatusCode = httpResponse.status
    client.close()
    logger.info("Received response with status: ${httpResponse.status}")
    println(httpResponseStatus)
    println(testSecret)
    transaction {
        SchemaUtils.create(ApiResponse)
        ApiResponse.insert {
            it[status] = httpResponseStatus.toString()
            it[response] = httpResponse.toString()
            it[createdAt] = LocalDateTime.now()
        }
    }
}