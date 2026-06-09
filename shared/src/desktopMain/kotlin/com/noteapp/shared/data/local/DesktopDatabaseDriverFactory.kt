package com.noteapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.noteapp.shared.database.NoteAppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:noteapp.db")
        NoteAppDatabase.Schema.create(driver)
        return driver
    }
}
