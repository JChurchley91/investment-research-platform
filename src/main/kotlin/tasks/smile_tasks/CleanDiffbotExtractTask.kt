package tasks.smile_tasks

import appConfig
import kotlinx.coroutines.coroutineScope
import models.api_cleanses.CleansedDiffbotExtract
import models.api_extracts.DiffbotExtract
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
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

                if (newsArticle != null) {
                    val newsArticleText: String =
                        newsArticle[DiffbotExtract.text].toString()
                    val newsArticleCleaned = cleanTextForNlp(newsArticleText)
                    logger.info("Cleaned Text For $symbol Article On $today")
                    transaction {
                        val existingCleansedArticle: Long =
                            CleansedDiffbotExtract
                                .select {
                                    CleansedDiffbotExtract.apiResponseKey eq
                                        "$symbol-$today"
                                }.count()
                        if (existingCleansedArticle > 0) {
                            logger.info("Data Already Exists For $symbol on $today")
                        } else {
                            CleansedDiffbotExtract.insert {
                                it[apiResponseKey] = symbolSearchKey
                                it[cleansedExtractedText] = newsArticleCleaned
                                it[createdAt] = today
                            }
                        }
                    }
                } else {
                    logger.info("No Diffbot Extract Exists for $symbol on $today")
                }
            }
        }
    }
}
