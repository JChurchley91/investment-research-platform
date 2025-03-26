import azure.DatabaseFactory
import config.databaseTables
import config.schemas
import config.tasksToSchedule
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scheduler.Scheduler

val logger: Logger = LoggerFactory.getLogger("Main")
val scheduler = Scheduler()

fun initializeDatabase() {
    try {
        DatabaseFactory.init()
        transaction {
            for (schema in schemas) {
                exec("CREATE SCHEMA IF NOT EXISTS $schema")
            }
            for (table in databaseTables) {
                SchemaUtils.create(table)
            }
        }
    } catch (exception: Exception) {
        logger.error("Error initializing database: $exception")
    }
}

fun initializeScheduler() {
    try {
        scheduler.start(tasksToSchedule)
    } catch (exception: Exception) {
        logger.error("Error initializing scheduler: $exception")
    }
}

fun main() {
    try {
        logger.info("Initializing Database; Creating Schemas & Tables")
        initializeDatabase()
        logger.info("Initializing Scheduler; Scheduling Tasks")
        initializeScheduler()
    } catch (exception: Exception) {
        println("Error occurred during application runtime: $exception")
    }
}
