package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_61_62 : Migration(61, 62) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
                UPDATE wallets SET type='View' WHERE type='view'
            """.trimIndent()
        )
        db.execSQL("""
                UPDATE wallets SET type='Single' WHERE type='single'
            """.trimIndent()
        )
        db.execSQL("""
                UPDATE wallets SET type='PrivateKey' WHERE type='private_key'
            """.trimIndent()
        )
        db.execSQL("""
                UPDATE wallets SET type='Multicoin' WHERE type='multicoin'
            """.trimIndent()
        )
    }
}