package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_50_51 : Migration(50, 51) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                ALTER TABLE prices ADD COLUMN usd_value REAL DEFAULT NUll
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE currency_rates (
                    currency TEXT NOT NULL,
                    rate REAL NOT NULL,
                    PRIMARY KEY(currency)
                )
            """.trimIndent()
        )
    }
}