package gcp

import org.jetbrains.exposed.sql.Database
import java.util.Properties

object DatabaseFactory {
    fun init() {
        val props =
            Properties().apply {
                load(Thread.currentThread().contextClassLoader.getResourceAsStream("database.properties"))
            }
        Database.connect(
            url = props.getProperty("db.url"),
            driver = "org.postgresql.Driver",
            user = props.getProperty("db.user"),
            password = props.getProperty("db.password"),
        )
    }
}
