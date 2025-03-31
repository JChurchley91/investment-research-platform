package tasks.smile_tasks

import appConfig
import kotlinx.coroutines.coroutineScope
import models.api_extracts.DiffbotExtract
import models.api_transforms.DiffbotExtractSplits
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import smile.nlp.sentences

/**
 * Task to split existing diffbot extracts into individual sentences.
 */
class SplitDiffbotExtractTask :
    SmileTask(
        taskName = "splitDiffbotExtract",
        taskSchedule = "20 9 * * *",
    ) {
    val listOfSymbols = appConfig.getSharePriceTickers() + appConfig.getCryptoCoins()

    /**
     * Retrieve existing diffbot extracts for today's date.
     */
    fun retrieveDiffbotExtracts(): List<ResultRow> =
        transaction {
            DiffbotExtract
                .select {
                    DiffbotExtract.createdAt eq today
                }.toList()
        }

    /**
     * Retrieve existing diffbot extracts and split them into sentences.
     * Save each sentence individually to the database.
     */
    suspend fun splitDiffbotExtract() {
        coroutineScope {
            for (symbol in listOfSymbols) {
                val diffbotExtractData: List<ResultRow> = retrieveDiffbotExtracts()
                val symbolSearchKey = "$symbol-$today"

                logger.info("Fetching Diffbot Extract Data for $symbol on $today")
                val newsArticle: ResultRow? =
                    diffbotExtractData.find { article ->
                        article[DiffbotExtract.apiResponseKey] == symbolSearchKey
                    }

                if (newsArticle != null) {
                    val newsArticleText: String =
                        newsArticle[DiffbotExtract.summary]
                            .toString()
                            .replace(":", " -")
                            .replace("\"", "")

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
                            val splitSentences: Array<String> = newsArticleText.sentences()
                            var sentenceCountId = 0
                            logger.info("Inserting ${splitSentences.size} Sentences For $symbol on $today")
                            for (sentence in splitSentences) {
                                sentenceCountId = sentenceCountId + 1
                                if (sentence.isNotEmpty()) {
                                    DiffbotExtractSplits.insert {
                                        it[diffbotExtractId] = "$symbolSearchKey-$sentenceCountId"
                                        it[apiResponseKey] = symbolSearchKey
                                        it[diffbotExtractSentence] = sentence
                                        it[createdAt] = today
                                    }
                                }
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
