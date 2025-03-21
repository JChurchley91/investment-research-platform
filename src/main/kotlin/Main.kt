import api.Prices
import api.TrendingNews
import gcp.DatabaseFactory
import models.ApiResponses
import models.ApiResponsesBody
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
            SchemaUtils.create(ApiResponsesBody)
        }
    } catch (exception: Exception) {
        logger.error("Error initializing database: $exception")
    }
}

fun main() {
    try {
        val trendingNews = TrendingNews()
        logger.info("Research Platform Initialized; Starting Application")

        logger.info("Initializing Database; Creating Schemas & Tables")
        initializeDatabase()

        logger.info("Initializing Scheduler; Scheduling Tasks")
        val scheduler = Scheduler()
        scheduler.start(
            listOf(
                Triple(
                    trendingNews::callApi,
                    trendingNews::taskSchedule,
                    trendingNews::taskName,
                ),
                Triple(
                    Prices()::callApi,
                    Prices()::taskSchedule,
                    Prices()::taskName,
                ),
            ),
        )
    } catch (exception: Exception) {
        println("Error occurred during application runtime: $exception")
    }
}
