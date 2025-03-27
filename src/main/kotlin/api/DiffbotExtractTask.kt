package api

import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import models.DailyNewsArticles
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

    fun getNewsArticles(): List<ResultRow> {
        return transaction {
            DailyNewsArticles.select {
                DailyNewsArticles.createdAt eq today
            }.toList()
        }
    }

    suspend fun callApi() {
        val newsArticlesToday: List<ResultRow> = getNewsArticles()

        for (symbol in listOfSymbols) {
            val symbolSearchKey = "$symbol-$yesterday"
            val newsArticle: ResultRow? = newsArticlesToday.find { article ->
                article[DailyNewsArticles.apiResponseKey] == symbolSearchKey
            }
            if (newsArticle != null) {
                logger.info("Fetching Diffbot Extract Data for $symbol; $yesterday")
                val newsArticleUrl: String = newsArticle[DailyNewsArticles.url].replace("\"", "")
                val httpResponse: HttpResponse = client.get("$apiUrl&token=$apiKey&url=$newsArticleUrl" +
                        "&naturalLanguage=entities,facts,categories,sentiment")
                val responseBody: String = httpResponse.body()
                println(responseBody)
            } else {
                logger.info("No News Article Exists for $symbol; $yesterday")
            }
        }
    }
}
