package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_57_58 : Migration(57, 58) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                
            """.trimIndent()
        )
    }
}