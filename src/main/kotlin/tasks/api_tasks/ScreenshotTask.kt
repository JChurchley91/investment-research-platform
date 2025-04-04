package tasks.api_tasks

import azure.SecretManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import models.api_extracts.DailyNewsArticles
import models.api_extracts.DailyNewsScreenshots
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Task to capture screenshots of daily news articles.
 */
class ScreenshotTask :
    ApiTask(
        taskName = "dailyNewsScreenshot",
        taskSchedule = "45 8 * * *",
        apiUrl = "https://screenshot.abstractapi.com/v1",
    ) {
    val apiKeyName: String = "abstract-api-key"
    var newsArticlesToday: List<ResultRow>? = null

    /**
     * Retrieves the news articles for today from the database.
     */
    fun getNewsArticles(): List<ResultRow> =
        transaction {
            DailyNewsArticles
                .select {
                    DailyNewsArticles.createdAt eq today
                }.toList()
        }

    fun insertImage(
        apiResponseKeyValue: String,
        apiResponseArticleKeyValue: String,
        imageBytes: ByteArray,
    ) {
        transaction {
            DailyNewsScreenshots.insert {
                it[apiResponseKey] = apiResponseKeyValue
                it[apiResponseArticleKey] = apiResponseArticleKeyValue
                it[imageData] = imageBytes
                it[createdAt] = today
            }
        }
    }

    suspend fun callApi() {
        val secretManager = SecretManager()
        val apiKey: String? = secretManager.getSecret(apiKeyName)
        newsArticlesToday = getNewsArticles()

        if (newsArticlesToday != null) {
            for (article in newsArticlesToday) {
                logger.info("Fetching Screenshot Data")
                val newsArticleUrl: String = article[DailyNewsArticles.url].replace("\"", "")
                val httpResponse: HttpResponse =
                    client.get("$apiUrl?url=$newsArticleUrl&api_key=$apiKey")
                insertApiResponse(article[DailyNewsArticles.title], httpResponse)
                val imageBytes: ByteArray = httpResponse.body()
                logger.info("Inserting Screenshot Data")
                insertImage(
                    article[DailyNewsArticles.apiResponseKey].toString(),
                    article[DailyNewsArticles.apiResponseArticleKey].toString(),
                    imageBytes,
                )
            }
        }
    }
}
