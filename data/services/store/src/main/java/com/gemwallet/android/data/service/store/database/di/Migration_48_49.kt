package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_48_49 : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE assets_priority (
                    query TEXT NOT NULL,
                    asset_id TEXT NOT NULL,
                    priority INT NOT NULL,
                    PRIMARY KEY(query, asset_id)
                )
            """.trimIndent()
        )
    }
}