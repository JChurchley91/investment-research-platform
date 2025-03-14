package azure

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Properties

object DatabaseFactory {
    fun init() {
        val props = Properties().apply {
            load(this::class.java.classLoader.getResourceAsStream("database.properties"))
        }
        Database.connect(
            url = props.getProperty("db.url"),
            driver = "org.postgresql.Driver",
            user = props.getProperty("db.user"),
            password = props.getProperty("db.password")
        )
    }

    fun <T> dbQuery(block: () -> T): T =
        transaction { block() }
}