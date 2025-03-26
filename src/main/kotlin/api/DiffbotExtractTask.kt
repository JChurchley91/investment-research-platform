package api

import models.DailyNewsArticles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.select

class DiffbotExtractTask :
    ApiTask(
        taskName = "diffbotExtract",
        taskSchedule = "* * * * *",
        apiKeyName = "diffbot_api_key",
        apiUrl = "https://api.diffbot.com/v3/article",
    ) {
    val newsArticlesToday = getNewsArticles()

    fun getNewsArticles(): List<ResultRow> {
        val articles = DailyNewsArticles.select {
            DailyNewsArticles.createdAt eq today
        }.toList()
        return articles
    }

    suspend fun callApi() {
        logger.info("Extracting Article Content from Diffbot API")
        }
    }
}
