package azure

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("DatabaseFactory")
val dotenv = dotenv()

/**
 * A simple test table.
 * This is created to test the database connection.
 */
object TestTable : Table() {
    val id = integer("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
}

/**
 * Connect to a PostgresSQL database using Exposed ORM.
 * Connects using credentials sourced from .env file.
 */
object DatabaseFactory {
    /**
     * Initializes the database connection.
     * @return true if the connection is successful, false otherwise.
     */
    fun init(): Boolean {
        return try {
            val db =
                Database.connect(
                    url = dotenv["DB_URL"],
                    driver = "org.postgresql.Driver",
                    user = dotenv["DB_USER"],
                    password = dotenv["DB_PASSWORD"],
                )

            // Execute a simple query to check the connection
            transaction(db) {
                SchemaUtils.createMissingTablesAndColumns(TestTable)
                logger.info("Database connection established successfully.")
            }
            return true
        } catch (exception: Exception) {
            logger.error("Failed to connect to the database: ${exception.message}")
            false
        }
    }
}
