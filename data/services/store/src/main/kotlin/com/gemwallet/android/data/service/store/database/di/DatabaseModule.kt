package com.gemwallet.android.data.service.store.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gemwallet.android.data.service.store.database.AccountsDao
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.BalancesDao
import com.gemwallet.android.data.service.store.database.BannersDao
import com.gemwallet.android.data.service.store.database.ConnectionsDao
import com.gemwallet.android.data.service.store.database.GemDatabase
import com.gemwallet.android.data.service.store.database.NftDao
import com.gemwallet.android.data.service.store.database.NodesDao
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.PricesDao
import com.gemwallet.android.data.service.store.database.SessionDao
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.service.store.database.TransactionsDao
import com.gemwallet.android.data.service.store.database.WalletsDao
import com.gemwallet.android.data.service.store.database.entities.SESSION_REQUEST
import com.wallet.core.primitives.Chain
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        .addMigrations(MIGRATION_12_14)
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
        .addMigrations(MIGRATION_26_27)
        .addMigrations(MIGRATION_27_28)
        .addMigrations(MIGRATION_28_29)
        .addMigrations(MIGRATION_29_30)
        .addMigrations(MIGRATION_30_31)
        .addMigrations(MIGRATION_31_32)
        .addMigrations(MIGRATION_32_33)
        .addMigrations(MIGRATION_33_34)
        .addMigrations(MIGRATION_34_35)
        .addMigrations(MIGRATION_35_36)
        .addMigrations(MIGRATION_36_37)
        .addMigrations(MIGRATION_37_38)
        .addMigrations(MIGRATION_38_39)
        .addMigrations(MIGRATION_39_40)
        .addMigrations(MIGRATION_40_41)
        .addMigrations(Migration_41_42)
        .addMigrations(Migration_42_43)
        .addMigrations(Migration_43_44)
        .addMigrations(Migration_44_45)
        .addMigrations(Migration_45_46)
        .addMigrations(Migration_46_47)
        .addMigrations(Migration_47_48)
        .addMigrations(Migration_48_49)
        .addMigrations(Migration_49_50)
        .addMigrations(Migration_50_51)
        .addMigrations(Migration_51_52)
        .addMigrations(Migration_52_53)
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
    fun provideTransactionsDao(db: GemDatabase): TransactionsDao = db.transactionsDao()

    @Singleton
    @Provides
    fun provideConnectionsDao(db: GemDatabase): ConnectionsDao = db.connectionsDao()

    @Singleton
    @Provides
    fun provideStakeDao(db: GemDatabase): StakeDao = db.stakeDao()

    @Singleton
    @Provides
    fun provideNodeDao(db: GemDatabase): NodesDao = db.nodeDao()

    @Singleton
    @Provides
    fun provideSessionDao(db: GemDatabase): SessionDao = db.sessionDao()

    @Singleton
    @Provides
    fun provideBannersDao(db: GemDatabase): BannersDao = db.bannersDao()

    @Singleton
    @Provides
    fun providePriceAlertsDao(db: GemDatabase): PriceAlertsDao = db.priceAlertsDao()

    @Singleton
    @Provides
    fun provideNFTDao(db: GemDatabase): NftDao = db.nftDao()

    @Singleton
    @Provides
    fun provideAssetsPriorityDao(db: GemDatabase): AssetsPriorityDao = db.assetsPriorityDao()
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

val MIGRATION_12_14 = object : Migration(12, 14) {
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

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE session (" +
                    "`id` INTEGER NOT NULL," +
                    "`wallet_id` TEXT NOT NULL," +
                    "`currency` TEXT NOT NULL," +
                    "PRIMARY KEY (`id`)" +
                    ")"
        )
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

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE assets ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT FALSE""")
    }
}

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW assets_info")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS asset_config (
                asset_id TEXT NOT NULL,
                wallet_id TEXT NOT NULL,
                is_pinned INTEGER NOT NULL DEFAULT FALSE,
                is_visible INTEGER NOT NULL DEFAULT TRUE,
                list_position INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (asset_id, wallet_id)
            )
            """.trimIndent()
        )
        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |            assets.owner_address as address,
            |            assets.id as id,
            |            assets.name as name,
            |            assets.symbol as symbol,
            |            assets.decimals as decimals,
            |            assets.type as type,
            |            assets.is_buy_enabled as isBuyEnabled,
            |            assets.is_swap_enabled as isSwapEnabled,
            |            assets.is_stake_enabled as isStakeEnabled,
            |            assets.staking_apr as stakingApr,
            |            assets.links as links,
            |            assets.market as market,
            |            assets.rank as assetRank,
            |            accounts.derivation_path as derivationPath,
            |            accounts.chain as chain,
            |            accounts.wallet_id as walletId,
            |            accounts.extendedPublicKey as extendedPublicKey,
            |            asset_config.is_pinned AS pinned,
            |            asset_config.is_visible AS visible,
            |            asset_config.list_position AS listPosition,
            |            session.currency AS priceCurrency,
            |            wallets.type AS walletType,
            |            wallets.name AS walletName,
            |            prices.value AS priceValue,
            |            prices.dayChanged AS priceDayChanges,
            |            balances.amount AS amount,
            |            balances.type AS balanceType
            |        FROM assets
            |        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
            |        JOIN wallets ON wallets.id = accounts.wallet_id
            |        JOIN session ON accounts.wallet_id = session.wallet_id AND session.id == 1
            |        LEFT JOIN balances ON assets.owner_address = balances.address AND assets.id = balances.asset_id
            |        LEFT JOIN prices ON assets.id = prices.assetId
            |        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin()
        )
//        db.execSQL("ALTER TABLE `assets` DROP COLUMN `is_pinned`;")
//        db.execSQL("ALTER TABLE `assets` DROP COLUMN `is_visible`;")
    }
}

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE banners (
                wallet_id TEXT NOT NULL,
                asset_id TEXT NOT NULL,
                state TEXT NOT NULL,
                event TEXT NOT NULL,
                PRIMARY KEY (wallet_id, asset_id)
            )""".trimIndent())
    }
}

val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `extended_txs`")
        db.execSQL("DROP TABLE IF EXISTS transactions;")
        db.execSQL("""
            CREATE TABLE transactions (
                `id` TEXT NOT NULL,
                `walletId` TEXT NOT NULL,
                `hash` TEXT NOT NULL,
                `assetId`TEXT NOT NULL,
                `feeAssetId` TEXT NOT NULL,
                `owner` TEXT NOT NULL,
                `recipient` TEXT NOT NULL,
                `contract` TEXT,
                `state` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `blockNumber` TEXT NOT NULL,
                `sequence` TEXT NOT NULL,
                `fee` TEXT NOT NULL,
                `value` TEXT NOT NULL,
                `payload` TEXT,
                `direction` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `metadata` TEXT,
                PRIMARY KEY (`id`, `walletId`)
            )""".trimIndent()
        )
        db.execSQL("""
            |CREATE VIEW `extended_txs` AS SELECT
            |            DISTINCT tx.id,
            |            tx.hash,
            |            tx.assetId,
            |            tx.feeAssetId,
            |            tx.owner,
            |            tx.recipient,
            |            tx.contract,
            |            tx.state,
            |            tx.type,
            |            tx.blockNumber,
            |            tx.sequence,
            |            tx.fee,
            |            tx.value,
            |            tx.payload,
            |            tx.metadata,
            |            tx.direction,
            |            tx.createdAt,
            |            tx.updatedAt,
            |            tx.walletId,
            |            assets.decimals as assetDecimals,
            |            assets.name as assetName,
            |            assets.type as assetType,
            |            assets.symbol as assetSymbol,
            |            feeAsset.decimals as feeDecimals,
            |            feeAsset.name as feeName,
            |            feeAsset.type as feeType,
            |            feeAsset.symbol as feeSymbol,
            |            prices.value as assetPrice,
            |            prices.dayChanged as assetPriceChanged,
            |            feePrices.value as feePrice,
            |            feePrices.dayChanged as feePriceChanged
            |        FROM transactions as tx 
            |            INNER JOIN assets ON tx.assetId = assets.id 
            |            INNER JOIN assets as feeAsset ON tx.feeAssetId = feeAsset.id 
            |            LEFT JOIN prices ON tx.assetId = prices.assetId
            |            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.assetId 
            |            WHERE tx.owner IN (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1) OR tx.recipient in (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1)
            |                AND tx.walletId in (SELECT wallet_id FROM session WHERE session.id = 1)
            |            GROUP BY tx.id
            """.trimMargin()
        )
    }
}

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE price_alerts (
                asset_id TEXT NOT NULL,
                enabled INTEGER NOT NULL,
                price REAL DEFAULT NULL,
                price_percent_change REAL DEFAULT NULL,
                price_direction TEXT DEFAULT NULL,
                PRIMARY KEY (asset_id)
            )""".trimIndent())
    }
}

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS balances;")
        db.execSQL("""
            CREATE TABLE balances (
                asset_id TEXT NOT NULL,
                owner TEXT NOT NULL,
                available TEXT NOT NULL,
                available_amount REAL NOT NULL,
                frozen TEXT NOT NULL,
                frozen_amount REAL NOT NULL,
                locked TEXT NOT NULL,
                locked_amount REAL NOT NULL,
                staked TEXT NOT NULL,
                staked_amount REAL NOT NULL,
                pending TEXT NOT NULL,
                pending_amount REAL NOT NULL,
                rewards TEXT NOT NULL,
                rewards_amount REAL NOT NULL,
                reserved TEXT NOT NULL,
                reserved_amount REAL NOT NULL,
                total_amount REAL NOT NULL,
                enabled INTEGER NOT NULL,
                hidden INTEGER NOT NULL,
                pinned INTEGER NOT NULL,
                updated_at  INTEGER,
                PRIMARY KEY (asset_id, owner)
            );
        """.trimIndent())
        db.execSQL("DROP TABLE IF EXISTS prices;")
        db.execSQL(
            """
                CREATE TABLE prices (
                    asset_id TEXT NOT NULL,
                    value REAL DEFAULT 0,
                    day_changed REAL DEFAULT 0,
                    currency TEXT NOT NULL,
                    PRIMARY KEY (asset_id)
                );
            """.trimIndent()
        )
        db.execSQL("DROP VIEW IF EXISTS `extended_txs`")
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |        assets.owner_address as address,
            |        assets.id as id,
            |        assets.name as name,
            |        assets.symbol as symbol,
            |        assets.decimals as decimals,
            |        assets.type as type,
            |        assets.is_buy_enabled as isBuyEnabled,
            |        assets.is_swap_enabled as isSwapEnabled,
            |        assets.is_stake_enabled as isStakeEnabled,
            |        assets.staking_apr as stakingApr,
            |        assets.links as links,
            |        assets.market as market,
            |        assets.rank as assetRank,
            |        accounts.derivation_path as derivationPath,
            |        accounts.chain as chain,
            |        accounts.wallet_id as walletId,
            |        accounts.extendedPublicKey as extendedPublicKey,
            |        asset_config.is_pinned AS pinned,
            |        asset_config.is_visible AS visible,
            |        asset_config.list_position AS listPosition,
            |        session.id AS sessionId,
            |        session.currency AS priceCurrency,
            |        wallets.type AS walletType,
            |        wallets.name AS walletName,
            |        prices.value AS priceValue,
            |        prices.day_changed AS priceDayChanges,
            |        balances.available AS balanceAvailable,
            |        balances.available_amount AS balanceAvailableAmount,
            |        balances.frozen AS balanceFrozen,
            |        balances.frozen_amount AS balanceFrozenAmount,
            |        balances.locked AS balanceLocked,
            |        balances.locked_amount AS balanceLockedAmount,
            |        balances.staked AS balanceStaked,
            |        balances.staked_amount AS balanceStakedAmount,
            |        balances.pending AS balancePending,
            |        balances.pending_amount AS balancePendingAmount,
            |        balances.rewards AS balanceRewards,
            |        balances.rewards_amount AS balanceRewardsAmount,
            |        balances.reserved AS balanceReserved,
            |        balances.reserved_amount AS balanceReservedAmount,
            |        balances.total_amount AS balanceTotalAmount,
            |        (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
            |        balances.enabled AS balanceEnabled,
            |        balances.hidden AS balanceHidden,
            |        balances.pinned AS balancePinned,
            |        balances.updated_at AS balanceUpdatedAt
            |        FROM assets
            |        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
            |        JOIN wallets ON wallets.id = accounts.wallet_id
            |        JOIN session ON accounts.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON assets.owner_address = balances.owner AND assets.id = balances.asset_id
            |        LEFT JOIN prices ON assets.id = prices.asset_id AND prices.currency = session.currency
            |        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin()
        )
        db.execSQL("""
            |CREATE VIEW `extended_txs` AS SELECT
            |            DISTINCT tx.id,
            |            tx.hash,
            |            tx.assetId,
            |            tx.feeAssetId,
            |            tx.owner,
            |            tx.recipient,
            |            tx.contract,
            |            tx.state,
            |            tx.type,
            |            tx.blockNumber,
            |            tx.sequence,
            |            tx.fee,
            |            tx.value,
            |            tx.payload,
            |            tx.metadata,
            |            tx.direction,
            |            tx.createdAt,
            |            tx.updatedAt,
            |            tx.walletId,
            |            assets.decimals as assetDecimals,
            |            assets.name as assetName,
            |            assets.type as assetType,
            |            assets.symbol as assetSymbol,
            |            feeAsset.decimals as feeDecimals,
            |            feeAsset.name as feeName,
            |            feeAsset.type as feeType,
            |            feeAsset.symbol as feeSymbol,
            |            prices.value as assetPrice,
            |            prices.day_changed as assetPriceChanged,
            |            feePrices.value as feePrice,
            |            feePrices.day_changed as feePriceChanged
            |        FROM transactions as tx 
            |            INNER JOIN assets ON tx.assetId = assets.id 
            |            INNER JOIN assets as feeAsset ON tx.feeAssetId = feeAsset.id 
            |            LEFT JOIN prices ON tx.assetId = prices.asset_id
            |            LEFT JOIN prices as feePrices ON tx.feeAssetId = feePrices.asset_id 
            |            WHERE tx.owner IN (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1) OR tx.recipient in (SELECT accounts.address FROM accounts, session
            |    WHERE accounts.wallet_id = session.wallet_id AND session.id = 1)
            |                AND tx.walletId in (SELECT wallet_id FROM session WHERE session.id = 1)
            |            GROUP BY tx.id
            """.trimMargin()
        )
    }
}

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |        assets.owner_address as address,
            |        assets.id as id,
            |        assets.name as name,
            |        assets.symbol as symbol,
            |        assets.decimals as decimals,
            |        assets.type as type,
            |        assets.is_buy_enabled as isBuyEnabled,
            |        assets.is_swap_enabled as isSwapEnabled,
            |        assets.is_stake_enabled as isStakeEnabled,
            |        assets.staking_apr as stakingApr,
            |        assets.links as links,
            |        assets.market as market,
            |        assets.rank as assetRank,
            |        accounts.derivation_path as derivationPath,
            |        accounts.chain as chain,
            |        accounts.wallet_id as walletId,
            |        accounts.extendedPublicKey as extendedPublicKey,
            |        asset_config.is_pinned AS pinned,
            |        asset_config.is_visible AS visible,
            |        asset_config.list_position AS listPosition,
            |        session.id AS sessionId,
            |        session.currency AS priceCurrency,
            |        wallets.type AS walletType,
            |        wallets.name AS walletName,
            |        prices.value AS priceValue,
            |        prices.day_changed AS priceDayChanges,
            |        balances.available AS balanceAvailable,
            |        balances.available_amount AS balanceAvailableAmount,
            |        balances.frozen AS balanceFrozen,
            |        balances.frozen_amount AS balanceFrozenAmount,
            |        balances.locked AS balanceLocked,
            |        balances.locked_amount AS balanceLockedAmount,
            |        balances.staked AS balanceStaked,
            |        balances.staked_amount AS balanceStakedAmount,
            |        balances.pending AS balancePending,
            |        balances.pending_amount AS balancePendingAmount,
            |        balances.rewards AS balanceRewards,
            |        balances.rewards_amount AS balanceRewardsAmount,
            |        balances.reserved AS balanceReserved,
            |        balances.reserved_amount AS balanceReservedAmount,
            |        balances.total_amount AS balanceTotalAmount,
            |        (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
            |        balances.enabled AS balanceEnabled,
            |        balances.hidden AS balanceHidden,
            |        balances.pinned AS balancePinned,
            |        balances.updated_at AS balanceUpdatedAt
            |        FROM assets
            |        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
            |        JOIN wallets ON wallets.id = accounts.wallet_id
            |        LEFT JOIN session ON accounts.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON assets.owner_address = balances.owner AND assets.id = balances.asset_id
            |        LEFT JOIN prices ON assets.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin())
    }
}

val MIGRATION_36_37 = object : Migration(36, 37) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE banners ADD COLUMN chain TEXT")
    }
}

val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |        assets.owner_address as address,
            |        assets.id as id,
            |        assets.name as name,
            |        assets.symbol as symbol,
            |        assets.decimals as decimals,
            |        assets.type as type,
            |        assets.is_buy_enabled as isBuyEnabled,
            |        assets.is_swap_enabled as isSwapEnabled,
            |        assets.is_stake_enabled as isStakeEnabled,
            |        assets.staking_apr as stakingApr,
            |        assets.links as links,
            |        assets.market as market,
            |        assets.rank as assetRank,
            |        accounts.derivation_path as derivationPath,
            |        accounts.chain as chain,
            |        accounts.wallet_id as walletId,
            |        accounts.extendedPublicKey as extendedPublicKey,
            |        asset_config.is_pinned AS pinned,
            |        asset_config.is_visible AS visible,
            |        asset_config.list_position AS listPosition,
            |        session.id AS sessionId,
            |        prices.currency AS priceCurrency,
            |        wallets.type AS walletType,
            |        wallets.name AS walletName,
            |        prices.value AS priceValue,
            |        prices.day_changed AS priceDayChanges,
            |        balances.available AS balanceAvailable,
            |        balances.available_amount AS balanceAvailableAmount,
            |        balances.frozen AS balanceFrozen,
            |        balances.frozen_amount AS balanceFrozenAmount,
            |        balances.locked AS balanceLocked,
            |        balances.locked_amount AS balanceLockedAmount,
            |        balances.staked AS balanceStaked,
            |        balances.staked_amount AS balanceStakedAmount,
            |        balances.pending AS balancePending,
            |        balances.pending_amount AS balancePendingAmount,
            |        balances.rewards AS balanceRewards,
            |        balances.rewards_amount AS balanceRewardsAmount,
            |        balances.reserved AS balanceReserved,
            |        balances.reserved_amount AS balanceReservedAmount,
            |        balances.total_amount AS balanceTotalAmount,
            |        (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
            |        balances.enabled AS balanceEnabled,
            |        balances.hidden AS balanceHidden,
            |        balances.pinned AS balancePinned,
            |        balances.updated_at AS balanceUpdatedAt
            |        FROM assets
            |        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
            |        JOIN wallets ON wallets.id = accounts.wallet_id
            |        LEFT JOIN session ON accounts.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON assets.owner_address = balances.owner AND assets.id = balances.asset_id
            |        LEFT JOIN prices ON assets.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin())
    }
}

val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP VIEW IF EXISTS `asset_info`")
        db.execSQL("DROP TABLE IF EXISTS assets")
        db.execSQL("""
            CREATE TABLE assets (
                owner_address TEXT NOT NULL, 
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                symbol TEXT NOT NULL,
                decimals INTEGER NOT NULL,
                type TEXT NOT NULL,
                chain TEXT NOT NULL,
                is_buy_enabled INTEGER NOT NULL,
                is_swap_enabled INTEGER NOT NULL,
                is_stake_enabled INTEGER NOT NULL,
                staking_apr REAL,
                links TEXT,
                market TEXT,
                rank INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY (`owner_address`, `id`)
            );
            CREATE INDEX IF NOT EXISTS `index_assets_owner_address` ON `assets` (`owner_address`);
            CREATE INDEX IF NOT EXISTS `index_assets_id` ON `assets` (`id`);
        """.trimIndent()
        )
        db.execSQL("""
            |CREATE VIEW `asset_info` AS SELECT
            |        assets.owner_address as address,
            |        assets.id as id,
            |        assets.name as name,
            |        assets.symbol as symbol,
            |        assets.decimals as decimals,
            |        assets.type as type,
            |        assets.is_buy_enabled as isBuyEnabled,
            |        assets.is_swap_enabled as isSwapEnabled,
            |        assets.is_stake_enabled as isStakeEnabled,
            |        assets.staking_apr as stakingApr,
            |        assets.links as links,
            |        assets.market as market,
            |        assets.rank as assetRank,
            |        accounts.derivation_path as derivationPath,
            |        accounts.chain as chain,
            |        accounts.wallet_id as walletId,
            |        accounts.extendedPublicKey as extendedPublicKey,
            |        asset_config.is_pinned AS pinned,
            |        asset_config.is_visible AS visible,
            |        asset_config.list_position AS listPosition,
            |        session.id AS sessionId,
            |        prices.currency AS priceCurrency,
            |        wallets.type AS walletType,
            |        wallets.name AS walletName,
            |        prices.value AS priceValue,
            |        prices.day_changed AS priceDayChanges,
            |        balances.available AS balanceAvailable,
            |        balances.available_amount AS balanceAvailableAmount,
            |        balances.frozen AS balanceFrozen,
            |        balances.frozen_amount AS balanceFrozenAmount,
            |        balances.locked AS balanceLocked,
            |        balances.locked_amount AS balanceLockedAmount,
            |        balances.staked AS balanceStaked,
            |        balances.staked_amount AS balanceStakedAmount,
            |        balances.pending AS balancePending,
            |        balances.pending_amount AS balancePendingAmount,
            |        balances.rewards AS balanceRewards,
            |        balances.rewards_amount AS balanceRewardsAmount,
            |        balances.reserved AS balanceReserved,
            |        balances.reserved_amount AS balanceReservedAmount,
            |        balances.total_amount AS balanceTotalAmount,
            |        (balances.total_amount * prices.value) AS balanceFiatTotalAmount,
            |        balances.updated_at AS balanceUpdatedAt
            |        FROM assets
            |        JOIN accounts ON accounts.address = assets.owner_address AND assets.id LIKE accounts.chain || '%'
            |        JOIN wallets ON wallets.id = accounts.wallet_id
            |        LEFT JOIN session ON accounts.wallet_id = session.wallet_id
            |        LEFT JOIN balances ON assets.owner_address = balances.owner AND assets.id = balances.asset_id
            |        LEFT JOIN prices ON assets.id = prices.asset_id AND prices.currency = (SELECT currency FROM session WHERE id = 1)
            |        LEFT JOIN asset_config ON assets.id = asset_config.asset_id AND wallets.id = asset_config.wallet_id
            """.trimMargin())
    }
}

val MIGRATION_39_40 = object : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE nft_collection (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                chain TEXT NOT NULL,
                contract_address TEXT NOT NULL,
                image_url TEXT NOT NULL,
                preview_image_url TEXT NOT NULL,
                original_image_url TEXT NOT NULL,
                is_verified INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (id)
            )""".trimIndent()
        )
        db.execSQL("""
            CREATE TABLE nft_asset (
                id TEXT NOT NULL,
                collection_id TEXT NOT NULL,
                token_id TEXT NOT NULL,
                token_type TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                chain TEXT NOT NULL,
                image_url TEXT NOT NULL,
                preview_image_url TEXT NOT NULL,
                original_image_url TEXT NOT NULL,
                PRIMARY KEY (id),
                FOREIGN KEY (collection_id) REFERENCES nft_collection(id) ON DELETE CASCADE
            )""".trimIndent()
        )
        db.execSQL("""
            CREATE TABLE nft_attributes (
                asset_id TEXT NOT NULL,
                name TEXT NOT NULL,
                value TEXT NOT NULL,
                PRIMARY KEY (asset_id, name),
                FOREIGN KEY (asset_id) REFERENCES nft_asset(id) ON DELETE CASCADE
            )""".trimIndent()
        )
        db.execSQL("""
            CREATE TABLE nft_association (
                asset_id TEXT NOT NULL,
                wallet_id TEXT NOT NULL,
                PRIMARY KEY (wallet_id, asset_id),
                FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
                FOREIGN KEY (asset_id) REFERENCES nft_asset(id) ON DELETE CASCADE
            )""".trimIndent()
        )
    }
}

val MIGRATION_40_41 = object : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE nft_collection_link (
                collection_id TEXT NOT NULL,
                name TEXT NOT NULL,
                url TEXT NOT NULL,
                PRIMARY KEY (collection_id, name),
                FOREIGN KEY (collection_id) REFERENCES nft_collection(id) ON DELETE CASCADE
            )""".trimIndent()
        )
    }
}