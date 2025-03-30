package tasks.smile_tasks

import appConfig
import kotlinx.coroutines.coroutineScope
import models.api_extracts.DiffbotExtract
import models.api_transforms.DiffbotExtractSplits
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SplitDiffbotExtractTask :
    SmileTask(
        taskName = "splitDiffbotExtract",
        taskSchedule = "* 12 * * *",
    ) {
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()

    fun retrieveDiffbotExtracts(): List<ResultRow> =
        transaction {
            DiffbotExtract
                .select {
                    DiffbotExtract.createdAt eq today
                }.toList()
        }

    suspend fun splitDiffbotExtract() {
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
                        newsArticle[DiffbotExtract.summary].toString()

                    transaction {
                        val existingSplitArticle: Long =
                            DiffbotExtractSplits
                                .select {
                                    DiffbotExtractSplits.apiResponseKey eq
                                        "$symbol-$today"
                                }.count()
                        if (existingSplitArticle > 0) {
                            logger.info("Data Already Exists For $symbol on $today")
                        } else {
                            // TODO
                        }
                    }
                } else {
                    logger.info("No Diffbot Extract Exists for $symbol on $today")
                }
            }
        }
    }
}
