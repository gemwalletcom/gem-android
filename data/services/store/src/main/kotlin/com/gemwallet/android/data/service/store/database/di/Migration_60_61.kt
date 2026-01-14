package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_60_61 : Migration(60, 61) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
                CREATE TABLE price_alerts_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    assetId TEXT NOT NULL,
                    enabled INTEGER NOT NULL,
                    price REAL,
                    pricePercentChange REAL,
                    priceDirection TEXT,
                    currency TEXT NOT NULL
                );
                """.trimIndent()
        )
        db.execSQL("""
                INSERT INTO price_alerts_new (assetId, enabled, price, pricePercentChange, priceDirection, currency)
                    SELECT asset_id, enabled, price, price_percent_change, price_direction, 'USD' as currency FROM price_alerts;
            """.trimIndent())

        db.execSQL("""DROP TABLE price_alerts;""")

        db.execSQL("""ALTER TABLE price_alerts_new RENAME TO price_alerts;""".trimIndent())
    }
}