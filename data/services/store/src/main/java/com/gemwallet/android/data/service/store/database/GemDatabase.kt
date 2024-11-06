package com.gemwallet.android.data.service.store.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gemwallet.android.data.service.store.database.entities.DbAccount
import com.gemwallet.android.data.service.store.database.entities.DbAsset
import com.gemwallet.android.data.service.store.database.entities.DbAssetConfig
import com.gemwallet.android.data.service.store.database.entities.DbAssetInfo
import com.gemwallet.android.data.service.store.database.entities.DbBalance
import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.gemwallet.android.data.service.store.database.entities.DbConnection
import com.gemwallet.android.data.service.store.database.entities.DbDelegationBase
import com.gemwallet.android.data.service.store.database.entities.DbDelegationValidator
import com.gemwallet.android.data.service.store.database.entities.DbNode
import com.gemwallet.android.data.service.store.database.entities.DbPrice
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import com.gemwallet.android.data.service.store.database.entities.DbSession
import com.gemwallet.android.data.service.store.database.entities.DbToken
import com.gemwallet.android.data.service.store.database.entities.DbTransaction
import com.gemwallet.android.data.service.store.database.entities.DbTransactionExtended
import com.gemwallet.android.data.service.store.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.service.store.database.entities.DbWallet

@Database(
    version = 37,
    entities = [
        DbWallet::class,
        DbAccount::class,
        DbAsset::class,
        DbBalance::class,
        DbPrice::class,
        DbToken::class,
        DbTransaction::class,
        DbTxSwapMetadata::class,
        DbConnection::class,
        DbDelegationValidator::class,
        DbDelegationBase::class,
        DbNode::class,
        DbSession::class,
        DbAssetConfig::class,
        DbBanner::class,
        DbPriceAlert::class,
    ],
    views = [
        DbAssetInfo::class,
        DbTransactionExtended::class,
    ]
)
abstract class GemDatabase : RoomDatabase() {
    abstract fun walletsDao(): WalletsDao

    abstract fun accountsDao(): AccountsDao

    abstract fun assetsDao(): AssetsDao

    abstract fun balancesDao(): BalancesDao

    abstract fun pricesDao(): PricesDao

    abstract fun tokensDao(): TokensDao

    abstract fun transactionsDao(): TransactionsDao

    abstract fun connectionsDao(): ConnectionsDao

    abstract fun stakeDao(): StakeDao

    abstract fun nodeDao(): NodesDao

    abstract fun sessionDao(): SessionDao

    abstract fun bannersDao(): BannersDao

    abstract fun priceAlertsDao(): PriceAlertsDao
}