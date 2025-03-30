package tasks.smile_tasks

import appConfig
import kotlinx.coroutines.coroutineScope
import models.DiffbotExtract
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class CleanDiffbotExtractTask :
    SmileTask(
        taskName = "diffbotExtractKeyword",
        taskSchedule = "* * * * *",
    ) {
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()

    fun retrieveDiffbotExtracts(): List<ResultRow> =
        transaction {
            DiffbotExtract
                .select {
                    DiffbotExtract.createdAt eq today
                }.toList()
        }

    suspend fun cleanDiffbotExtracts() {
        coroutineScope {
            for (symbol in listOfSymbols) {
                val diffbotExtractData: List<ResultRow> = retrieveDiffbotExtracts()
                val symbolSearchKey = "$symbol-$today"
                val newsArticle: ResultRow? =
                    diffbotExtractData.find { article ->
                        article[DiffbotExtract.apiResponseKey] == symbolSearchKey
                    }
                val newsArticleText: String =
                    newsArticle?.get(DiffbotExtract.text).toString()
                val newsArticleCleaned = cleanTextForNlp(newsArticleText)
                println(newsArticleCleaned)
            }
        }
    }
}
