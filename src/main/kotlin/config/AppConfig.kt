package config

import azure.DatabaseFactory
import models.api_extracts.ApiResponses
import models.api_extracts.DailyCoinPrices
import models.api_extracts.DailyNewsArticles
import models.api_extracts.DailySharePrices
import models.api_extracts.DiffbotExtract
import models.api_transforms.DiffbotExtractSplits
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler
import tasks.api_tasks.CoinPricesTask
import tasks.api_tasks.DailyNewsTask
import tasks.api_tasks.DiffbotExtractTask
import tasks.api_tasks.SharePricesTask
import tasks.smile_tasks.SplitDiffbotExtractTask

val logger: Logger = LoggerFactory.getLogger("AppConfig")

/**
 * Configuration class for the application.
 * Contains methods to initialize the database, scheduler, and other configurations.
 */
class AppConfig {
    fun getSchemas(): List<String> =
        listOf(
            "raw",
            "transformed",
        )

    /**
     * Manually updated list of database tables saved as exposed models.
     * @return List of database tables to be created on initialization.
     */
    fun getDatabaseTables(): List<IntIdTable> =
        listOf(
            ApiResponses,
            DailyNewsArticles,
            DailyCoinPrices,
            DailySharePrices,
            DiffbotExtract,
            DiffbotExtractSplits,
        )

    /**
     * Manually updated list of share price tickers.
     * @return List of share price tickers to be used in the application.
     */
    fun getSharePriceTickers(): List<String> =
        listOf(
            "AAPL",
            "GOOGL",
            "MSFT",
            "AMZN",
            "META",
        )

    /**
     * Manually updated list of crypto coins.
     * @return List of crypto coins to be used in the application.
     */
    fun getCryptoCoins(): List<String> =
        listOf(
            "BTC",
            "ETH",
            "ADA",
            "XRP",
            "SOL",
        )

    /**
     * Manually updated list of tasks to be scheduled upon application startup.
     * @return List of tasks to be scheduled.
     */
    fun getTasksToSchedule(): List<TaskConfig> =
        listOf(
            TaskConfig(
                CoinPricesTask()::callApi,
                CoinPricesTask()::taskName,
                CoinPricesTask()::taskSchedule,
                CoinPricesTask()::cryptoCoins,
            ),
            TaskConfig(
                SharePricesTask()::callApi,
                SharePricesTask()::taskName,
                SharePricesTask()::taskSchedule,
                SharePricesTask()::sharePriceTickers,
            ),
            TaskConfig(
                DailyNewsTask()::callApi,
                DailyNewsTask()::taskName,
                DailyNewsTask()::taskSchedule,
                DailyNewsTask()::listOfSymbols,
            ),
            TaskConfig(
                DiffbotExtractTask()::callApi,
                DiffbotExtractTask()::taskName,
                DiffbotExtractTask()::taskSchedule,
                DiffbotExtractTask()::listOfSymbols,
            ),
            TaskConfig(
                SplitDiffbotExtractTask()::splitDiffbotExtract,
                SplitDiffbotExtractTask()::taskName,
                SplitDiffbotExtractTask()::taskSchedule,
                SplitDiffbotExtractTask()::listOfSymbols,
            ),
        )

    /**
     * Initializes the database connection and creates schemas and tables.
     * This method is called during application startup.
     */
    fun initializeDatabase() {
        try {
            if (DatabaseFactory.init()) {
                transaction {
                    for (schema in getSchemas()) {
                        exec("CREATE SCHEMA IF NOT EXISTS $schema")
                    }
                    for (table in getDatabaseTables()) {
                        SchemaUtils.create(table)
                    }
                }
            }
        } catch (exception: Exception) {
            logger.error("Error initializing database: $exception")
        }
    }

    /**
     * Initializes the scheduler and schedules tasks.
     * This method is called during application startup.
     */
    fun initializeScheduler() {
        try {
            scheduler.start(getTasksToSchedule())
        } catch (exception: Exception) {
            logger.error("Error initializing scheduler: $exception")
        }
    }
}
