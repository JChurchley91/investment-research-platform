package config

import azure.DatabaseFactory
import models.ApiResponses
import models.DailyCoinPrices
import models.DailyNewsArticles
import models.DailySharePrices
import models.DiffbotExtract
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler
import tasks.CoinPricesTask
import tasks.DailyNewsTask
import tasks.DiffbotExtractTask
import tasks.SharePricesTask

val logger: Logger = LoggerFactory.getLogger("AppConfig")

class AppConfig {
    fun getSchemas(): List<String> =
        listOf(
            "raw",
        )

    fun getDatabaseTables(): List<IntIdTable> =
        listOf(
            ApiResponses,
            DailyNewsArticles,
            DailyCoinPrices,
            DailySharePrices,
            DiffbotExtract,
        )

    fun getSharePriceTickers(): List<String> =
        listOf(
            "AAPL",
            "GOOGL",
            "MSFT",
            "AMZN",
            "META",
        )

    fun getCryptoCoins(): List<String> =
        listOf(
            "BTC",
            "ETH",
            "ADA",
            "XRP",
            "SOL",
        )

    fun getTasksToSchedule(): List<TaskConfig> =
        listOf(
            TaskConfig(
                DailyNewsTask()::callApi,
                DailyNewsTask()::taskSchedule,
                DailyNewsTask()::taskName,
                DailyNewsTask()::sharePriceTickers,
            ),
            TaskConfig(
                CoinPricesTask()::callApi,
                CoinPricesTask()::taskSchedule,
                CoinPricesTask()::taskName,
                CoinPricesTask()::cryptoCoins,
            ),
            TaskConfig(
                SharePricesTask()::callApi,
                SharePricesTask()::taskSchedule,
                SharePricesTask()::taskName,
                SharePricesTask()::sharePriceTickers,
            ),
            TaskConfig(
                DiffbotExtractTask()::callApi,
                DiffbotExtractTask()::taskSchedule,
                DiffbotExtractTask()::taskName,
                DiffbotExtractTask()::listOfSymbols,
            ),
        )

    fun initializeDatabase() {
        try {
            DatabaseFactory.init()
            transaction {
                for (schema in getSchemas()) {
                    exec("CREATE SCHEMA IF NOT EXISTS $schema")
                }
                for (table in getDatabaseTables()) {
                    SchemaUtils.create(table)
                }
            }
        } catch (exception: Exception) {
            logger.error("Error initializing database: $exception")
        }
    }

    fun initializeScheduler() {
        try {
            scheduler.start(getTasksToSchedule())
        } catch (exception: Exception) {
            logger.error("Error initializing scheduler: $exception")
        }
    }
}
