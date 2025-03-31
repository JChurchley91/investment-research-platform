package tasks.api_tasks

import azure.SecretManager
import config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import models.api_extracts.DailyNewsArticles
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Task to fetch and store daily news articles from the Alpha Vantage API.
 */
class DailyNewsTask :
    ApiTask(
        taskName = "dailyNewsSearch",
        taskSchedule = "10 9 * * *",
        apiUrl = "https://www.alphavantage.co/query?function=NEWS_SENTIMENT",
    ) {
    val appConfig: AppConfig = AppConfig()
    val sharePriceTickers: List<String> = appConfig.getSharePriceTickers()
    val cryptoCoins: List<String> = appConfig.getCryptoCoins()
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()
    val apiKeyName: String = "alpha-vantage-key"

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
        sourceDomainValue: String,
        overallSentimentLabelValue: String,
    ) {
        logger.info("Inserting News Article Data For $tickerValue")
        DailyNewsArticles.insert {
            it[apiResponseKey] = "$tickerValue-$today"
            it[title] = titleValue
            it[task] = taskName
            it[url] = urlValue
            it[sourceDomain] = sourceDomainValue
            it[overallSentimentLabel] = overallSentimentLabelValue
            it[createdAt] = today
        }
    }

    /**
     * Insert either the share price or coin API response into the database.
     * @param item The item to be inserted.
     * @param itemType The type of the item (either "sharePrices" or "cryptoCoins").
     * @param apiKey The API key for authentication.
     */
    suspend fun processShareTickerValueOrCoin(
        item: String,
        itemType: String,
        apiKey: String,
    ) {
        logger.info("Fetching News Articles for $item")
        if (itemType != "sharePrices" && itemType != "cryptoCoins") {
            logger.error("Invalid item type; Must be either 'sharePrices' or 'cryptoCoins'")
            return
        } else {
            val httpResponse: HttpResponse =
                if (itemType == "sharePrices") {
                    client.get(
                        "$apiUrl&tickers=$item&sort=LATEST&limit=1&apikey=$apiKey",
                    )
                } else {
                    client.get(
                        "$apiUrl&tickers=CRYPTO:$item&sort=LATEST&limit=1&apikey=$apiKey",
                    )
                }

            val responseBody: String = httpResponse.body()
            val newsSentimentFeed: NewsSentimentFeed = defaultJson.decodeFromString(responseBody)
            val topNewsSentimentFeed = newsSentimentFeed.newsSentimentFeed[0]

            transaction {
                if (checkExistingApiResponse(item)) {
                    logger.info("Data already exists for $item on $today")
                    return@transaction
                } else {
                    insertApiResponse(item, httpResponse)
                    insertNewsArticle(
                        item,
                        topNewsSentimentFeed["title"].toString(),
                        topNewsSentimentFeed["url"].toString(),
                        topNewsSentimentFeed["source_domain"].toString(),
                        topNewsSentimentFeed["overall_sentiment_label"].toString(),
                    )
                }
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
        for (item in sharePriceTickers) {
            processShareTickerValueOrCoin(item, "sharePrices", apiKey)
        }
        for (item in cryptoCoins) {
            processShareTickerValueOrCoin(item, "cryptoCoins", apiKey)
        }
    }
}
