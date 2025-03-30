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
        taskSchedule = "15 9 * * *",
        apiUrl = "https://api.diffbot.com/v3/article?",
    ) {
    val appConfig: AppConfig = AppConfig()
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()
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
     * @param htmlValue The HTML content of the article.
     */
    fun insertDiffbotExtract(
        symbol: String,
        htmlValue: String,
        summaryValue: String,
    ) {
        logger.info("Inserting Diffbot Extract Data for $symbol; $today")
        DiffbotExtract.insert {
            it[apiResponseKey] = "$symbol-$today"
            it[task] = taskName
            it[summary] = summaryValue
            it[html] = htmlValue
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
     */
    suspend fun callApi() {
        val newsArticlesToday: List<ResultRow> = getNewsArticles()
        val secretManager = SecretManager()
        val apiKey: String? = secretManager.getSecret(apiKeyName)

        for (symbol in listOfSymbols) {
            val symbolSearchKey = "$symbol-$today"
            val newsArticle: ResultRow? =
                newsArticlesToday.find { article ->
                    article[DailyNewsArticles.apiResponseKey] == symbolSearchKey
                }
            if (newsArticle != null) {
                logger.info("Fetching Diffbot Extract Data for $symbol; $today")
                val newsArticleUrl: String = newsArticle[DailyNewsArticles.url].replace("\"", "")
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
                    if (checkExistingApiResponse(symbol)) {
                        logger.info("Data already exists for $symbol on $today")
                        return@transaction
                    } else {
                        insertApiResponse(symbol, httpResponse)
                        insertDiffbotExtract(
                            symbol,
                            topDiffbotExtractObject["html"].toString(),
                            diffbotExtractNaturalLanguage["summary"].toString().replace("\"", ""),
                        )
                    }
                }
            } else {
                logger.info("No News Article Exists for $symbol; $today")
            }
        }
    }
}
