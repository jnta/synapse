package dev.synapse.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databaseFile = File(System.getProperty("user.home"), ".synapse/synapse.db")
        if (!databaseFile.parentFile.exists()) {
            databaseFile.parentFile.mkdirs()
        }
        val databaseExists = databaseFile.exists()
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        if (!databaseExists) {
            SynapseDatabase.Schema.create(driver)
        }
        return driver
    }
}
