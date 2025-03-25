import api.DailyPrices
import api.TrendingNews
import azure.DatabaseFactory
import models.ApiResponses
import models.DailyCoinPrices
import models.TrendingNewsArticles
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler.Scheduler

val logger: Logger = LoggerFactory.getLogger("Main")

fun initializeDatabase() {
    try {
        DatabaseFactory.init()
        transaction {
            exec("CREATE SCHEMA IF NOT EXISTS raw")
            SchemaUtils.create(ApiResponses)
            SchemaUtils.create(TrendingNewsArticles)
            SchemaUtils.create(DailyCoinPrices)
        }
    } catch (exception: Exception) {
        logger.error("Error initializing database: $exception")
    }
}

fun main() {
    try {
        logger.info("Research Platform Initialized; Starting Application")

        logger.info("Initializing Database; Creating Schemas & Tables")
        initializeDatabase()

        logger.info("Initializing Scheduler; Scheduling Tasks")
        val scheduler = Scheduler()
        scheduler.start(
            listOf(
                Triple(
                    TrendingNews()::callApi,
                    TrendingNews()::taskSchedule,
                    TrendingNews()::taskName,
                ),
                Triple(
                    DailyPrices()::callApi,
                    DailyPrices()::taskSchedule,
                    DailyPrices()::taskName,
                ),
            ),
        )
    } catch (exception: Exception) {
        println("Error occurred during application runtime: $exception")
    }
}
