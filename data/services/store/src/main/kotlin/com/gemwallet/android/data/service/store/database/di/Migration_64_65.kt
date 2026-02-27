package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_64_65 : Migration(64, 65) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeHigh REAL""".trimIndent())
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeHighDate INTEGER""".trimIndent())
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeHighChangePercentage REAL""".trimIndent())
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeLow REAL""".trimIndent())
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeLowDate INTEGER""".trimIndent())
        db.execSQL(""" ALTER TABLE asset_market ADD COLUMN allTimeLowChangePercentage REAL""".trimIndent())
    }
}