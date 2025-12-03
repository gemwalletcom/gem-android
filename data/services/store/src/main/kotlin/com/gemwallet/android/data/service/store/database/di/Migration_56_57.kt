package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_56_57 : Migration(56, 57) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE recent_log (
                    asset_id TEXT NOT NULL,
                    wallet_id TEXT NOT NULL,
                    to_asset_id TEXT,
                    type TEXT NOT NULL,
                    loggedAt INTEGER NOT NULL,
                    PRIMARY KEY(asset_id, wallet_id, type),
                    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE CASCADE,
                    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
                );
            """.trimIndent()
        )
    }
}