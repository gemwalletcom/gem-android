package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_66_67 : Migration(66, 67) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                DROP TABLE IF EXISTS 'stake_delegation_validator'
            """.trimIndent()
        )
        db.execSQL(
            """
                DROP TABLE IF EXISTS 'nft_collection'
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE stake_delegation_validator (
                    id TEXT NOT NULL,
                    chain TEXT NOT NULL,
                    name TEXT NOT NULL,
                    isActive INTEGER NOT NULL,
                    commission REAL NOT NULL,
                    apr REAL NOT NULL,
                    providerType TEXT,
                    PRIMARY KEY(id)
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE nft_collection (
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    chain TEXT NOT NULL,
                    contractAddress TEXT NOT NULL,
                    imageUrl TEXT NOT NULL,
                    previewImageUrl TEXT NOT NULL,
                    originalSourceUrl TEXT NOT NULL,
                    status TEXT,
                    PRIMARY KEY(id)
                )
            """.trimIndent()
        )
    }
}