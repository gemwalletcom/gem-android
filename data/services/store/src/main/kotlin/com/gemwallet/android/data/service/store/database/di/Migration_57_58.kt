package com.gemwallet.android.data.service.store.database.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_57_58 : Migration(57, 58) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE perpetual (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                provider TEXT NOT NULL,
                assetId TEXT NOT NULL,
                identifier TEXT NOT NULL,
                price REAL NOT NULL,
                pricePercentChange24h REAL NOT NULL,
                openInterest REAL NOT NULL,
                volume24h REAL NOT NULL,
                funding REAL NOT NULL,
                maxLeverage INTEGER NOT NULL,
                PRIMARY KEY (id)
            )
        """.trimIndent())
        db.execSQL("""CREATE INDEX IF NOT EXISTS `perpetual_asset_id_idx` ON `perpetual` (`assetId`)""")
        db.execSQL("""
            CREATE TABLE perpetual_metadata (
                perpetualId TEXT NOT NULL,
                isPinned INTEGER NOT NULL,
                PRIMARY KEY(perpetualId)
            )
        """.trimIndent())
        db.execSQL("""        
            CREATE TABLE perpetual_balance (
                accountAddress TEXT NOT NULL,
                available REAL NOT NULL,
                reserved REAL NOT NULL,
                withdrawable REAL NOT NULL,
                PRIMARY KEY(accountAddress)
            );
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE perpetual_position(
                id TEXT NOT NULL,
                perpetualId TEXT NOT NULL,
                accountAddress TEXT NOT NULL,
                assetId TEXT NOT NULL,
                size REAL NOT NULL,
                sizeValue REAL NOT NULL,
                leverage INTEGER NOT NULL,
                entryPrice REAL,
                liquidationPrice REAL,
                marginType TEXT NOT NULL,
                direction TEXT NOT NULL,
                marginAmount REAL NOT NULL,
                takeProfitPrice REAL,
                takeProfitType TEXT,
                takeProfitOrderId TEXT,
                stopLossPrice REAL,
                stopLossType TEXT,
                stopLossOrderId TEXT,
                pnl REAL NOT NULL,
                funding REAL,
                PRIMARY KEY(id, perpetualId, accountAddress)
            )
        """.trimIndent())
        db.execSQL("""CREATE INDEX IF NOT EXISTS `perpetual_position_asset_id_idx` ON `perpetual_position` (`assetId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `perpetual_position_perpetual_id_idx` ON `perpetual_position` (`perpetualId`)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS `perpetual_position_account_address_idx` ON `perpetual_position` (`accountAddress`)""")
    }
}