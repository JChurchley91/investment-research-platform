package tasks.api_tasks

import azure.SecretManager
import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import models.api_extracts.DailyNewsArticles
import models.api_extracts.DiffbotExtract
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Task to fetch and store extracts from the Diffbot API.
 */
class DiffbotExtractTask :
    ApiTask(
        taskName = "diffbotExtract",
        taskSchedule = "10 9 * * *",
        apiUrl = "https://api.diffbot.com/v3/article?",
    ) {
    val appConfig: AppConfig = AppConfig()
    val cryptoCoins = appConfig.getCryptoCoins()
    val apiKeyName: String = "diffbot-api-key"

    /**
     * Data class representing the response from the Diffbot API.
     */
    @Serializable
    data class DiffBotExtractObjects(
        @SerialName("objects") val objects: List<JsonObject>,
    )

    /**
     * Inserts the Diffbot API response into the database.
     *
     * @param symbol The symbol of the item.
     * @param summaryValue The summary of the extracted news article.
     * @param apiResponseArticleKeyValue The key used to store the article in the raw database.
     */
    fun insertDiffbotExtract(
        symbol: String,
        summaryValue: String,
        apiResponseArticleKeyValue: String,
    ) {
        logger.info("Inserting Diffbot Extract Data for $symbol; $today")
        DiffbotExtract.insert {
            it[apiResponseKey] = "$symbol-$today"
            it[apiResponseArticleKey] = apiResponseArticleKeyValue
            it[task] = taskName
            it[summary] = summaryValue
            it[createdAt] = today
        }
    }

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

    /**
     * Calls the Diffbot API to fetch extracts for the news articles.
     * Stores the summary of the news article into the enhanced schema of the database.
     */
    suspend fun callApi() {
        val newsArticlesToday: List<ResultRow> = getNewsArticles()
        val secretManager = SecretManager()
        val apiKey: String? = secretManager.getSecret(apiKeyName)

        for (coin in cryptoCoins) {
            val symbolSearchKey = "$coin-$today"
            val relevantNewsArticles =
                newsArticlesToday.filter { article ->
                    article[DailyNewsArticles.apiResponseKey] == symbolSearchKey
                }

            if (relevantNewsArticles.isNotEmpty()) {
                logger.info("Fetching Diffbot Extract Data for $coin; $today")
                relevantNewsArticles.forEach { article ->
                    logger.info("Fetching Diffbot Extract Data")
                    val newsArticleUrl: String = article[DailyNewsArticles.url].replace("\"", "")
                    val httpResponse: HttpResponse =
                        client.get(
                            "$apiUrl&token=$apiKey&naturalLanguage=summary" +
                                "&summaryNumSentences=5&url=$newsArticleUrl",
                        )
                    val responseBody: String = httpResponse.body()
                    val diffbotExtractObject: DiffBotExtractObjects = defaultJson.decodeFromString(responseBody)
                    val topDiffbotExtractObject = diffbotExtractObject.objects[0]
                    val diffbotExtractNaturalLanguage: JsonObject =
                        topDiffbotExtractObject["naturalLanguage"]
                            as JsonObject

                    transaction {
                        insertApiResponse(coin, httpResponse)
                        insertDiffbotExtract(
                            coin,
                            diffbotExtractNaturalLanguage["summary"].toString().replace("\"", ""),
                            article[DailyNewsArticles.apiResponseArticleKey].toString(),
                        )
                    }
                }
            }
        }
    }
}
