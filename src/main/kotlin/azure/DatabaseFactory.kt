package azure

import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties

val logger: Logger = LoggerFactory.getLogger("DatabaseFactory")

object DatabaseFactory {
    fun init(): Boolean {
        val props =
            Properties().apply {
                load(Thread.currentThread().contextClassLoader.getResourceAsStream("database.properties"))
            }
        try {
            Database.connect(
                url = props.getProperty("db.url"),
                driver = "org.postgresql.Driver",
                user = props.getProperty("db.user"),
                password = props.getProperty("db.password"),
            )
            logger.info("Database connection established successfully.")
            return true
        } catch (exception: Exception) {
            logger.info("Failed to connect to the database: ${exception.message}")
            return false
        }
    }
}
