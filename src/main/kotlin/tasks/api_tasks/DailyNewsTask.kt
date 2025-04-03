package tasks.api_tasks

import azure.SecretManager
import config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import models.api_extracts.DailyNewsArticles
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Task to fetch and store daily news articles from the Alpha Vantage API.
 */
class DailyNewsTask :
    ApiTask(
        taskName = "dailyNewsSearch",
        taskSchedule = "5 9 * * *",
        apiUrl = "https://www.alphavantage.co/query?function=NEWS_SENTIMENT",
    ) {
    val appConfig: AppConfig = AppConfig()
    val cryptoCoins: List<String> = appConfig.getCryptoCoins()
    val apiKeyName: String = "alpha-vantage-key"
    val timeFrom: String = today.minusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"))

    /**
     * Data class representing the response from the Alpha Vantage API.
     */
    @Serializable
    data class NewsSentimentFeed(
        @SerialName("feed") val newsSentimentFeed: List<JsonObject>,
    )

    /**
     * Insert the news article API response into the database.
     * @param tickerValue The ticker value of the news article.
     * @param titleValue The title of the news article.
     * @param urlValue The URL of the news article.
     * @param sourceDomainValue The source domain of the news article.
     * @param overallSentimentLabelValue The overall sentiment label of the news article.
     */
    fun insertNewsArticle(
        tickerValue: String,
        titleValue: String,
        urlValue: String,
        apiResponseArticleKeyValue: Int,
        sourceDomainValue: String,
        overallSentimentScoreValue: Double,
        overallSentimentLabelValue: String,
    ) {
        logger.info("Inserting News Article Data For $tickerValue")
        DailyNewsArticles.insert {
            it[apiResponseKey] = "$tickerValue-$today"
            it[title] = titleValue
            it[task] = taskName
            it[apiResponseArticleKey] = "$tickerValue-$today-$apiResponseArticleKeyValue"
            it[url] = urlValue
            it[sourceDomain] = sourceDomainValue
            it[overallSentimentLabel] = overallSentimentLabelValue
            it[overallSentimentScore] = overallSentimentScoreValue
            it[createdAt] = today
        }
    }

    /**
     * Insert the daily news for a given coin into the database.
     * Inserts the top 5 news articles for each coin.
     * @param item The item to be inserted.
     * @param apiKey The API key for authentication.
     */
    suspend fun processCryptoCoinArticle(
        item: String,
        apiKey: String,
    ) {
        val httpResponse: HttpResponse =
            client.get(
                "$apiUrl&tickers=CRYPTO:$item&time_from=$timeFrom" +
                        "&sort=RELEVANCE&limit=5&apikey=$apiKey",
            )

        val responseBody: String = httpResponse.body()
        val newsSentimentFeed: NewsSentimentFeed = defaultJson.decodeFromString(responseBody)
        val top5NewsSentimentFeed = newsSentimentFeed.newsSentimentFeed.take(5)

        transaction {
            insertApiResponse(item, httpResponse)
            var articleValueKey = 0
            top5NewsSentimentFeed.forEach { newsSentimentFeed ->
                articleValueKey = articleValueKey + 1
                insertNewsArticle(
                    item,
                    newsSentimentFeed["title"].toString(),
                    newsSentimentFeed["url"].toString(),
                    articleValueKey,
                    newsSentimentFeed["source_domain"].toString(),
                    newsSentimentFeed["overall_sentiment_score"]!!.jsonPrimitive.double,
                    newsSentimentFeed["overall_sentiment_label"].toString(),
                )
            }
        }
    }

    /**
     * Calls the Alpha Vantage API to fetch daily news articles.
     * Stores the response in the database.
     */
    suspend fun callApi() {
        logger.info("Calling API; Fetching News Articles")
        val secretManager = SecretManager()
        val apiKey: String = secretManager.getSecret(apiKeyName)
        for (item in cryptoCoins) {
            logger.info("Fetching News Articles for $item")
            processCryptoCoinArticle(item, apiKey)
        }
    }
}
