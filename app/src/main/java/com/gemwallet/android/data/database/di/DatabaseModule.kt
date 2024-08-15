package com.gemwallet.android.data.database.di

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gemwallet.android.data.bridge.ConnectionsDao
import com.gemwallet.android.data.config.NodeDao
import com.gemwallet.android.data.config.OfflineFirstConfigRepository
import com.gemwallet.android.data.database.AssetsDao
import com.gemwallet.android.data.database.BalancesDao
import com.gemwallet.android.data.database.GemDatabase
import com.gemwallet.android.data.database.PricesDao
import com.gemwallet.android.data.database.SessionDao
import com.gemwallet.android.data.database.entities.SESSION_REQUEST
import com.gemwallet.android.data.repositories.session.SessionSharedPreferenceSource
import com.gemwallet.android.data.stake.StakeDao
import com.gemwallet.android.data.tokens.TokensDao
import com.gemwallet.android.data.transaction.TransactionsDao
import com.gemwallet.android.data.wallet.AccountsDao
import com.gemwallet.android.data.wallet.WalletsDao
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context): GemDatabase = Room.databaseBuilder(
            context = context,
            klass = GemDatabase::class.java,
            name = "gem.db",
        )
        .addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .addMigrations(MIGRATION_3_4)
        .addMigrations(MIGRATION_4_5)
        .addMigrations(MIGRATION_5_6)
        .addMigrations(MIGRATION_6_7)
        .addMigrations(MIGRATION_7_8)
        .addMigrations(MIGRATION_8_9)
        .addMigrations(MIGRATION_9_10)
        .addMigrations(MIGRATION_10_11)
        .addMigrations(MIGRATION_11_12)
        .addMigrations(MIGRATION_12_14(context))
        .addMigrations(MIGRATION_14_15)
        .addMigrations(MIGRATION_15_16)
        .addMigrations(MIGRATION_16_17)
        .addMigrations(MIGRATION_17_18)
        .addMigrations(MIGRATION_18_19)
        .addMigrations(MIGRATION_19_20)
        .addMigrations(MIGRATION_20_21)
        .addMigrations(MIGRATION_21_23)
        .addMigrations(MIGRATION_23_24)
        .addMigrations(MIGRATION_24_25)
        .addMigrations(MIGRATION_25_26)
        .addMigrations(MIGRATION_26_27(context))
        .addMigrations(MIGRATION_27_28)
        .addMigrations(MIGRATION_28_29)
        .build()

    @Singleton
    @Provides
    fun provideWalletsDao(db: GemDatabase): WalletsDao = db.walletsDao()

    @Singleton
    @Provides
    fun provideAccountsDao(db: GemDatabase): AccountsDao = db.accountsDao()

    @Singleton
    @Provides
    fun provideAssetsDao(db: GemDatabase): AssetsDao = db.assetsDao()

    @Singleton
    @Provides
    fun provideBalancesDao(db: GemDatabase): BalancesDao = db.balancesDao()

    @Singleton
    @Provides
    fun providePricesDao(db: GemDatabase): PricesDao = db.pricesDao()

    @Singleton
    @Provides
    fun provideTokensDao(db: GemDatabase): TokensDao = db.tokensDao()

    @Singleton
    @Provides
    fun provideTransactionsDao(db: GemDatabase): TransactionsDao = db.transactionsDao()

    @Singleton
    @Provides
    fun provideConnectionsDao(db: GemDatabase): ConnectionsDao = db.connectionsDao()

    @Singleton
    @Provides
    fun provideStakeDao(db: GemDatabase): StakeDao = db.stakeDao()

    @Singleton
    @Provides
    fun provideNodeDao(db: GemDatabase): NodeDao = db.nodeDao()

    @Singleton
    @Provides
    fun provideSessionDao(db: GemDatabase): SessionDao = db.sessionDao()
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE transfer_transactions (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`hash` TEXT NOT NULL," +
                "`assetId` TEXT NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`recipient` TEXT NOT NULL," +
                "`contract` TEXT," +
                "`state` TEXT NOT NULL," +
                "`blockNumber` INTEGER NOT NULL," +
                "`sequence` INTEGER NOT NULL," +
                "`fee` TEXT NOT NULL," +
                "`value` TEXT NOT NULL," +
                "`payload` TEXT" +
                ")")
    }

}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS transfer_transactions;")
        db.execSQL("CREATE TABLE transfer_transactions (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`hash` TEXT NOT NULL," +
                "`assetId` TEXT NOT NULL," +
                "`feeAssetId` TEXT NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`recipient` TEXT NOT NULL," +
                "`contract` TEXT," +
                "`state` TEXT NOT NULL," +
                "`blockNumber` INTEGER NOT NULL," +
                "`sequence` INTEGER NOT NULL," +
                "`fee` TEXT NOT NULL," +
                "`value` TEXT NOT NULL," +
                "`payload` TEXT," +
                "`direction` TEXT NOT NULL," +
                "`updatedAt` INTEGER NOT NULL," +
                "`createdAt` INTEGER NOT NULL" +
                ")")
    }

}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS transfer_transactions;")
        db.execSQL("CREATE TABLE transfer_transactions (" +
                "`hash` TEXT NOT NULL," +
                "`assetId`TEXT NOT NULL," +
                "`feeAssetId` TEXT NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`recipient` TEXT NOT NULL," +
                "`contract` TEXT," +
                "`state` TEXT NOT NULL," +
                "`blockNumber` INTEGER NOT NULL," +
                "`sequence` INTEGER NOT NULL," +
                "`fee` TEXT NOT NULL," +
                "`value` TEXT NOT NULL," +
                "`payload` TEXT," +
                "`direction` TEXT NOT NULL," +
                "`updatedAt` INTEGER NOT NULL," +
                "`createdAt` INTEGER NOT NULL," +
                "PRIMARY KEY (`hash`, `assetId`)" +
                ")")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM assets")
        db.execSQL("DELETE FROM balances")
        db.execSQL("DELETE FROM prices")
        db.execSQL("DELETE FROM tokens")
        db.execSQL("DELETE FROM transfer_transactions")
        db.execSQL("DELETE FROM accounts")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE wallets ADD COLUMN domain_name TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM transfer_transactions")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS transfer_transactions;")
        db.execSQL("CREATE TABLE transactions (" +
                "`id` TEXT NOT NULL," +
                "`hash` TEXT NOT NULL," +
                "`assetId`TEXT NOT NULL," +
                "`feeAssetId` TEXT NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`recipient` TEXT NOT NULL," +
                "`contract` TEXT," +
                "`state` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`blockNumber` INTEGER NOT NULL," +
                "`sequence` INTEGER NOT NULL," +
                "`fee` TEXT NOT NULL," +
                "`value` TEXT NOT NULL," +
                "`payload` TEXT," +
                "`direction` TEXT NOT NULL," +
                "`updatedAt` INTEGER NOT NULL," +
                "`createdAt` INTEGER NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ")")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS transactions;")
        db.execSQL("CREATE TABLE transactions (" +
                "`id` TEXT NOT NULL," +
                "`hash` TEXT NOT NULL," +
                "`assetId`TEXT NOT NULL," +
                "`feeAssetId` TEXT NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`recipient` TEXT NOT NULL," +
                "`contract` TEXT," +
                "`state` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`blockNumber` TEXT NOT NULL," +
                "`sequence` TEXT NOT NULL," +
                "`fee` TEXT NOT NULL," +
                "`value` TEXT NOT NULL," +
                "`payload` TEXT," +
                "`direction` TEXT NOT NULL," +
                "`updatedAt` INTEGER NOT NULL," +
                "`createdAt` INTEGER NOT NULL," +
                "PRIMARY KEY (`id`)" +
                ")")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE wallets ADD COLUMN `index` INTEGER NOT NULL DEFAULT 0")
        val result = db.query("SELECT id FROM wallets")
        var index = 1
        while (result.moveToNext()) {
            val id = result.getString(0)
            db.execSQL("UPDATE wallets SET `index`=$index WHERE id = \"$id\"")
            index++
        }
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS room_connection;")
        db.execSQL("CREATE TABLE room_connection (" +
                "`id` TEXT NOT NULL," +
                "`wallet_id` TEXT NOT NULL," +
                "`session_id`TEXT NOT NULL," +
                "`state` TEXT NOT NULL," +
                "`created_at` INTEGER NOT NULL," +
                "`expire_at` INTEGER NOT NULL," +
                "`app_name` TEXT NOT NULL," +
                "`app_description` TEXT NOT NULL," +
                "`app_url` TEXT NOT NULL," +
                "`app_icon` TEXT NOT NULL," +
                "`redirect_native` TEXT," +
                "`redirect_universal` TEXT," +
                "PRIMARY KEY (`id`)" +
                ")")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        db.execSQL("ALTER TABLE assets ADD COLUMN is_buy_enabled INTEGER NOT NULL DEFAULT FALSE")
        db.execSQL("ALTER TABLE assets ADD COLUMN is_swap_enabled INTEGER NOT NULL DEFAULT FALSE")
        db.execSQL("ALTER TABLE assets ADD COLUMN is_stake_enabled INTEGER NOT NULL DEFAULT FALSE")
        db.setTransactionSuccessful()
        db.endTransaction()
    }
}

class MIGRATION_12_14(private val context: Context) : Migration(12, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE tx_swap_metadata (" +
                    "`tx_id` TEXT NOT NULL," +
                    "`from_asset_id` TEXT NOT NULL," +
                    "`to_asset_id` TEXT NOT NULL," +
                    "`from_amount` TEXT NOT NULL," +
                    "`to_amount` TEXT NOT NULL," +
                    "PRIMARY KEY (`tx_id`)" +
                ")"
        )
        db.execSQL("DELETE FROM transactions")
        OfflineFirstConfigRepository(context = context).setTxSyncTime(0L)
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        db.execSQL("ALTER TABLE assets ADD COLUMN links TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN market TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN rank INTEGER NOT NULL DEFAULT 0")
        db.setTransactionSuccessful()
        db.endTransaction()
        db.execSQL("DELETE FROM assets WHERE type = 'ARBITRUM'")
        db.execSQL("DELETE FROM tokens WHERE type = 'ARBITRUM'")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM transactions")
        db.execSQL("DELETE FROM tokens")
        db.execSQL("DELETE FROM prices")
        db.execSQL("UPDATE assets SET id = '${Chain.Xrp.string}' WHERE id = 'ripple' ")
        db.execSQL("UPDATE assets SET id = '${Chain.Xrp.string}' WHERE id = 'Ripple' ")
        db.execSQL("UPDATE balances SET asset_id = '${Chain.Xrp.string}' WHERE asset_id = 'Ripple'")
        db.execSQL("UPDATE accounts SET chain = '${Chain.Xrp.name}' WHERE chain = 'ripple'")
        db.execSQL("UPDATE accounts SET chain = '${Chain.Xrp.name}' WHERE chain = 'Ripple'")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE stake_delegation_validator (" +
                    "`id` TEXT NOT NULL," +
                    "`chain` TEXT NOT NULL," +
                    "`name` TEXT NOT NULL," +
                    "`is_active` INTEGER NOT NULL DEFAULT 0," +
                    "`commission` REAL NOT NULL," +
                    "`apr` REAL NOT NULL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
        db.execSQL(
            "CREATE TABLE stake_delegation_base (" +
                    "`id` TEXT NOT NULL," +
                    "`address` TEXT NOT NULL," +
                    "`delegation_id` TEXT NOT NULL," +
                    "`validator_id` TEXT NOT NULL," +
                    "`asset_id` TEXT NOT NULL," +
                    "`state` TEXT NOT NULL," +
                    "`balance` TEXT NOT NULL," +
                    "`completion_date` INTEGER," +
                    "`price` REAL," +
                    "`price_change` REAL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS stake_delegation_validator;")
        db.execSQL("DROP TABLE IF EXISTS stake_delegation_base;")
        db.execSQL(
            "CREATE TABLE stake_delegation_validator (" +
                    "`id` TEXT NOT NULL," +
                    "`chain` TEXT NOT NULL," +
                    "`name` TEXT NOT NULL," +
                    "`is_active` INTEGER NOT NULL DEFAULT 0," +
                    "`commission` REAL NOT NULL," +
                    "`apr` REAL NOT NULL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
        db.execSQL(
            "CREATE TABLE stake_delegation_base (" +
                    "`id` TEXT NOT NULL," +
                    "`address` TEXT NOT NULL," +
                    "`delegation_id` TEXT NOT NULL," +
                    "`validator_id` TEXT NOT NULL," +
                    "`asset_id` TEXT NOT NULL," +
                    "`state` TEXT NOT NULL," +
                    "`balance` TEXT NOT NULL," +
                    "`rewards` TEXT NOT NULL," +
                    "`completion_date` INTEGER," +
                    "`price` REAL," +
                    "`price_change` REAL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE assets ADD COLUMN staking_apr REAL")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM tokens")
        db.execSQL("ALTER TABLE tokens ADD COLUMN rank INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM transactions")
        db.execSQL("ALTER TABLE transactions ADD COLUMN metadata TEXT")
    }
}

val MIGRATION_21_23 = object : Migration(21, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM assets WHERE type='BEP2'")
        db.execSQL("DELETE FROM accounts WHERE chain='binance'")
        db.execSQL("DELETE FROM accounts WHERE chain='Binance'")
        db.execSQL("DELETE FROM transactions")
        db.execSQL("DELETE FROM tokens")
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE balances ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE stake_delegation_base ADD COLUMN shares TEXT DEFAULT NULL")
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE nodes (" +
                "`url` TEXT NOT NULL," +
                "`status` TEXT NOT NULL," +
                "`priority` INTEGER NOT NULL," +
                "`chain` TEXT NOT NULL," +
                "PRIMARY KEY (`url`)" +
            ")")
    }
}

class MIGRATION_26_27(private val context: Context) : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE session (" +
                    "`id` INTEGER NOT NULL," +
                    "`wallet_id` TEXT NOT NULL," +
                    "`currency` TEXT NOT NULL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
        val source = SessionSharedPreferenceSource(context)
        val walletId = source.getWalletId() ?: return
        val currency = source.getCurrency()
        val values = ContentValues().apply {
            this.put("id", 1)
            this.put("wallet_id", walletId)
            this.put("currency", currency.string)
        }
        val result = db.insert("session", SQLiteDatabase.CONFLICT_REPLACE, values)
        Log.d("MIGRATION", "Result: $result")
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
           CREATE VIEW `assets_info` AS SELECT
            assets.*,
            accounts.*,
            session.currency AS priceCurrency,
            wallets.type AS walletType,
            wallets.name AS walletName,
            prices.value AS priceValue,
            prices.dayChanged AS priceDayChanges,
            balances.amount AS amount,
            balances.type as balanceType
        FROM assets
        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
        JOIN wallets ON wallets.id = accounts.wallet_id
        JOIN session ON accounts.wallet_id = session.wallet_id AND session.id == 1
        LEFT JOIN balances ON assets.owner_address = balances.address AND assets.id = balances.asset_id
        LEFT JOIN prices ON assets.id = prices.assetId 
        """)
    }
}

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
        CREATE VIEW `extended_txs` AS SELECT
            DISTINCT tx.id,
            tx.hash,
            tx.assetId,
            tx.feeAssetId,
            tx.owner,
            tx.recipient,
            tx.contract,
            tx.state,
            tx.type,
            tx.blockNumber,
            tx.sequence,
            tx.fee,
            tx.value,
            tx.payload,
            tx.metadata,
            tx.direction,
            tx.createdAt,
            tx.updatedAt,
            assets.decimals as assetDecimals,
            assets.name as assetName,
            assets.type as assetType,
            assets.symbol as assetSymbol,
            feeAsset.decimals as feeDecimals,
            feeAsset.name as feeName,
            feeAsset.type as feeType,
            feeAsset.symbol as feeSymbol,
            prices.value as assetPrice,
            prices.dayChanged as assetPriceChanged,
            feePrices.value as feePrice,
            feePrices.dayChanged as feePriceChanged
        FROM transactions as tx 
            INNER JOIN assets ON tx.assetId = assets.id 
            INNER JOIN assets as feeAsset ON tx.feeAssetId = feeAsset.id 
            LEFT JOIN prices ON tx.assetId = prices.assetId
            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.assetId 
            WHERE tx.owner IN ($SESSION_REQUEST) OR tx.recipient in ($SESSION_REQUEST)
            GROUP BY tx.id
        """)
    }
}