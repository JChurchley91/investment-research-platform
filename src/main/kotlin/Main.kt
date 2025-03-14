import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.LoggerFactory

val logger: org.slf4j.Logger = LoggerFactory.getLogger("news-fetcher")

suspend fun main() {
    logger.info("Sending request to ktor.io")
    val client = HttpClient(CIO)
    val response: HttpResponse = client.get("https://ktor.io/")
    println(response.status)
    logger.info("Received response with status: ${response.status}")
    client.close()
}