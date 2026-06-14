package com.noteapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.noteapp.shared.database.NoteAppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:noteapp.db")
        val tableExists = try {
            driver.executeQuery(
                null,
                "SELECT name FROM sqlite_master WHERE type='table' AND name='NoteEntity'",
                { app.cash.sqldelight.db.QueryResult.Value(it.getString(0) != null) },
                0
            ).value
        } catch (e: Exception) { false }
        if (!tableExists) {
            NoteAppDatabase.Schema.create(driver)
        }
        return driver
    }
}
