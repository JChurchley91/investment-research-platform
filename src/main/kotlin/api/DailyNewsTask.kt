package api

import config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import models.DailyNewsArticles
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class DailyNewsTask :
    ApiTask(
        taskName = "dailyNewsSearch",
        taskSchedule = "* * * * *",
        apiKeyName = "alpha-vantage-key",
        apiUrl = "https://www.alphavantage.co/query?function=NEWS_SENTIMENT",
    ) {
    val appConfig: AppConfig = AppConfig()
    val sharePriceTickers: List<String> = appConfig.getSharePriceTickers()
    val cryptoCoins: List<String> = appConfig.getCryptoCoins()

    @Serializable
    data class NewsSentimentFeed(
        @SerialName("feed") val newsSentimentFeed: List<JsonObject>,
    )

    fun insertNewsArticle(
        tickerValue: String,
        titleValue: String,
        urlValue: String,
        sourceDomainValue: String,
        overallSentimentLabelValue: String,
    ) {
        logger.info("Inserting news article data for $tickerValue")
        DailyNewsArticles.insert {
            it[apiResponseKey] = "$tickerValue-$yesterday"
            it[title] = titleValue
            it[task] = taskName
            it[url] = urlValue
            it[sourceDomain] = sourceDomainValue
            it[overallSentimentLabel] = overallSentimentLabelValue
            it[createdAt] = LocalDateTime.now()
        }
    }

    suspend fun processShareTickerValueOrCoin(
        item: String,
        itemType: String,
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
                    logger.info("Data already exists for $item on $yesterday")
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

    suspend fun callApi() {
        logger.info("Calling API; Fetching News Articles")
        for (item in sharePriceTickers) {
            processShareTickerValueOrCoin(item, "sharePrices")
        }
        for (item in cryptoCoins) {
            processShareTickerValueOrCoin(item, "cryptoCoins")
        }
    }
}
