import api.TrendingNews
import org.slf4j.LoggerFactory
import scheduler.Scheduler

fun main() {
    try {
        val logger = LoggerFactory.getLogger("Main")
        val trendingNews = TrendingNews()
        logger.info("Research Platform Initialized; Starting Application")

        logger.info("Initializing Database; Creating Schemas & Tables")
        trendingNews.initialize()

        logger.info("Initializing Scheduler; Scheduling Tasks")
        val scheduler = Scheduler()
        scheduler.start(
            listOf(
                Triple(
                    trendingNews::callApi,
                    trendingNews::taskSchedule,
                    trendingNews::taskName,
                ),
            ),
        )
    } catch (exception: Exception) {
        println("Error occurred during application runtime: $exception")
    }
}
