package tasks

import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import models.DailyNewsArticles
import models.DiffbotExtract
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DiffbotExtractTask :
    ApiTask(
        taskName = "diffbotExtract",
        taskSchedule = "* * * * *",
        apiKeyName = "diffbot-api-key",
        apiUrl = "https://api.diffbot.com/v3/article?",
    ) {
    val appConfig: AppConfig = AppConfig()
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()

    @Serializable
    data class DiffBotExtractObjects(
        @SerialName("objects") val objects: List<JsonObject>,
    )

    fun insertDiffbotExtract(
        symbol: String,
        htmlValue: String,
        textValue: String,
    ) {
        logger.info("Inserting Diffbot Extract Data for $symbol; $today")
        DiffbotExtract.insert {
            it[apiResponseKey] = "$symbol-$today"
            it[task] = taskName
            it[html] = htmlValue
            it[text] = textValue
            it[createdAt] = today
        }
    }

    fun getNewsArticles(): List<ResultRow> =
        transaction {
            DailyNewsArticles
                .select {
                    DailyNewsArticles.createdAt eq today
                }.toList()
        }

    suspend fun callApi() {
        val newsArticlesToday: List<ResultRow> = getNewsArticles()

        for (symbol in listOfSymbols) {
            val symbolSearchKey = "$symbol-$today"
            val newsArticle: ResultRow? =
                newsArticlesToday.find { article ->
                    article[DailyNewsArticles.apiResponseKey] == symbolSearchKey
                }
            if (newsArticle != null) {
                logger.info("Fetching Diffbot Extract Data for $symbol; $today")
                val newsArticleUrl: String = newsArticle[DailyNewsArticles.url].replace("\"", "")
                val httpResponse: HttpResponse = client.get("$apiUrl&token=$apiKey&url=$newsArticleUrl")
                val responseBody: String = httpResponse.body()
                val diffbotExtractObject: DiffBotExtractObjects = defaultJson.decodeFromString(responseBody)
                val topDiffbotExtractObject = diffbotExtractObject.objects[0]

                transaction {
                    if (checkExistingApiResponse(symbol)) {
                        logger.info("Data already exists for $symbol on $today")
                        return@transaction
                    } else {
                        insertApiResponse(symbol, httpResponse)
                        insertDiffbotExtract(
                            symbol,
                            topDiffbotExtractObject["html"].toString(),
                            topDiffbotExtractObject["text"].toString(),
                        )
                    }
                }
            } else {
                logger.info("No News Article Exists for $symbol; $today")
            }
        }
    }
}
