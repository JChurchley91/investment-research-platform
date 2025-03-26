package config

import api.CoinPricesTask
import api.DailyNewsTask
import api.SharePricesTask
import azure.DatabaseFactory
import models.ApiResponses
import models.DailyCoinPrices
import models.DailySharePrices
import models.TrendingNewsArticles
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler

val logger: Logger = LoggerFactory.getLogger("AppConfig")

class AppConfig {
    fun getSchemas(): List<String> =
        listOf(
            "raw",
        )

    fun getDatabaseTables(): List<IntIdTable> =
        listOf(
            ApiResponses,
            TrendingNewsArticles,
            DailyCoinPrices,
            DailySharePrices,
        )

    fun getSharePriceTickers(): List<String> =
        listOf(
            "AAPL",
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
