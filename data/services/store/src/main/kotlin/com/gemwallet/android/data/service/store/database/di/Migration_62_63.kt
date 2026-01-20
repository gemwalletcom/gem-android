package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_62_63 : Migration(62, 63) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
                ALTER TABLE price_alerts ADD COLUMN lastNotifiedAt INTEGER  
            """.trimIndent()
        )
    }
}