package com.gemwallet.android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gemwallet.android.data.bridge.ConnectionsDao
import com.gemwallet.android.data.bridge.RoomConnection
import com.gemwallet.android.data.config.NodeDao
import com.gemwallet.android.data.config.RoomNode
import com.gemwallet.android.data.database.entities.DbAsset
import com.gemwallet.android.data.database.entities.DbAssetConfig
import com.gemwallet.android.data.database.entities.DbAssetInfo
import com.gemwallet.android.data.database.entities.DbBalance
import com.gemwallet.android.data.database.entities.DbBanner
import com.gemwallet.android.data.database.entities.DbPrice
import com.gemwallet.android.data.database.entities.DbSession
import com.gemwallet.android.data.database.entities.DbToken
import com.gemwallet.android.data.database.entities.DbTransaction
import com.gemwallet.android.data.database.entities.DbTransactionExtended
import com.gemwallet.android.data.database.entities.DbTxSwapMetadata
import com.gemwallet.android.data.stake.RoomDelegationBase
import com.gemwallet.android.data.stake.RoomDelegationValidator
import com.gemwallet.android.data.stake.StakeDao
import com.gemwallet.android.data.tokens.TokensDao
import com.gemwallet.android.data.wallet.AccountRoom
import com.gemwallet.android.data.wallet.AccountsDao
import com.gemwallet.android.data.wallet.WalletRoom
import com.gemwallet.android.data.wallet.WalletsDao

@Database(
    version = 33,
    entities = [
        WalletRoom::class,
        AccountRoom::class,
        DbAsset::class,
        DbBalance::class,
        DbPrice::class,
        DbToken::class,
        DbTransaction::class,
        DbTxSwapMetadata::class,
        RoomConnection::class,
        RoomDelegationValidator::class,
        RoomDelegationBase::class,
        RoomNode::class,
        DbSession::class,
        DbAssetConfig::class,
        DbBanner::class,
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

    abstract fun nodeDao(): NodeDao

    abstract fun sessionDao(): SessionDao

    abstract fun bannersDao(): BannersDao
}