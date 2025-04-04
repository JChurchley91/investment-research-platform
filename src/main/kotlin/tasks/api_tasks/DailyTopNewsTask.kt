package tasks.api_tasks

import azure.SecretManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import models.api_extracts.DailyTopNews
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A task responsible for fetching and inserting daily top news articles using an external API.
 * This task interacts with a news API and stores the articles in a database.
 */
class DailyTopNewsTask :
    ApiTask(
        taskName = "dailyTopNews",
        taskSchedule = "* * * * *",
        apiUrl = "https://api.worldnewsapi.com/top-news",
    ) {
    val apiKeyName: String = "world-news-api"
    val newsMarkets: List<String> = listOf("us")

    /**
     * Represents the top news feed data structure received from an external API.
     */
    @Serializable
    data class TopNewsFeed(
        @SerialName("top_news") val topNewsFeed: List<JsonObject>,
    )

    /**
     * Inserts a top news article into the database.
     *
     * @param countryValue The country associated with the news article.
     * @param articleValue The unique identifier for the article within the context of the country and date.
     * @param urlValue The URL of the news article.
     * @param titleValue The title of the news article.
     */
    fun insertTopNewsArticle(
        countryValue: String,
        articleValue: Int,
        urlValue: String,
        titleValue: String,
    ) {
        DailyTopNews.insert {
            it[apiResponseKey] = "topNews-$today"
            it[apiResponseArticleKey] = "$countryValue-$today-$articleValue"
            it[url] = urlValue
            it[title] = titleValue
            it[createdAt] = today
        }
    }

    /**
     * Performs an API call to retrieve the top news articles for specific markets
     * and inserts the retrieved data into the database.
     */
    suspend fun callApi() {
        val secretManager = SecretManager()
        val apiKey: String = secretManager.getSecret(apiKeyName)

        for (market in newsMarkets) {
            logger.info("Retrieving Top News In $market For Today")
            val httpResponse: HttpResponse =
                client.get(
                    "$apiUrl?source-country=$market" +
                        "&language=en&headlines-only=true" +
                        "&date=$today&api-key=$apiKey",
                )
            val responseBody: String = httpResponse.body()
            val topNews: TopNewsFeed = defaultJson.decodeFromString(responseBody)
            val topNewsFiltered: JsonArray = topNews.topNewsFeed[0]["news"] as JsonArray
            logger.info("Inserting Top News In $market For Today")
            transaction {
                var index = 0
                topNewsFiltered.forEach { news ->
                    index = index + 1
                    insertTopNewsArticle(
                        countryValue = market,
                        articleValue = index,
                        urlValue = news.jsonObject["url"]!!.jsonPrimitive.contentOrNull!!,
                        titleValue = news.jsonObject["title"]!!.jsonPrimitive.contentOrNull!!,
                    )
                }
            }
        }
    }
}
