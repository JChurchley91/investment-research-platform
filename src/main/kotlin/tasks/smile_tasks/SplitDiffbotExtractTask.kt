package tasks.smile_tasks

import appConfig
import kotlinx.coroutines.coroutineScope
import models.api_enhancements.DiffbotExtractSplits
import models.api_extracts.DiffbotExtract
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import smile.nlp.sentences
import kotlin.collections.forEach

/**
 * Task to split existing diffbot extracts into individual sentences.
 */
class SplitDiffbotExtractTask :
    SmileTask(
        taskName = "splitDiffbotExtract",
        taskSchedule = "15 9 * * *",
    ) {
    val cryptoCoins = appConfig.getCryptoCoins()

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
            for (coin in cryptoCoins) {
                val diffbotExtractData: List<ResultRow> = retrieveDiffbotExtracts()
                val symbolSearchKey = "$coin-$today"

                logger.info("Fetching Diffbot Extract Data for $coin on $today")
                val relevantNewsArticles =
                    diffbotExtractData.filter { article ->
                        article[DiffbotExtract.apiResponseKey] == symbolSearchKey
                    }

                if (relevantNewsArticles.isNotEmpty()) {
                    transaction {
                        relevantNewsArticles.forEach { article ->
                            logger.info("Splitting news article for $article[DiffbotExtract.apiResponseArticleKey]")
                            val newsArticleText: String =
                                article[DiffbotExtract.summary]
                                    .toString()
                                    .replace(":", " -")
                                    .replace("\"", "")
                            val splitSentences: Array<String> = newsArticleText.sentences()
                            logger.info(
                                "Inserting ${splitSentences.size} Sentences For " +
                                    "$article[DiffbotExtract.apiResponseArticleKey]",
                            )
                            splitSentences.filter { it.isNotEmpty() }.forEachIndexed { index, sentence ->
                                DiffbotExtractSplits.insert {
                                    it[diffbotExtractId] = "$symbolSearchKey-${index + 1}"
                                    it[apiResponseKey] = symbolSearchKey
                                    it[apiResponseArticleKey] = article[DiffbotExtract.apiResponseArticleKey]
                                    it[diffbotExtractSentence] = sentence
                                    it[createdAt] = today
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
