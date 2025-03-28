import config.AppConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler.Scheduler

val appConfig: AppConfig = AppConfig()
val logger: Logger = LoggerFactory.getLogger("Main")
val scheduler = Scheduler()

/**
 * Main function to execute the application.
 * Initializes the database and scheduler.
 */
fun main() {
    try {
        logger.info("Initializing Database; Creating Schemas & Tables")
        appConfig.initializeDatabase()
        logger.info("Initializing Scheduler; Scheduling Tasks")
        appConfig.initializeScheduler()
    } catch (exception: Exception) {
        logger.error("Error occurred during application runtime: $exception")
    }
}
