package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

@Dao
interface BannersDao {
    @Query("SELECT * FROM banners WHERE wallet_id=:walletId AND (asset_id=:assetId OR asset_id LIKE :chain || '%') AND event = :event")
    suspend fun getBanner(walletId: String?, assetId: String?, chain: String?, event: BannerEvent): DbBanner?

    @Query("""
        SELECT * FROM
            banners
        WHERE
            wallet_id=:walletId AND (asset_id=:assetId OR chain=:chain) AND state IN (:state)
    """)
    suspend fun getBanner(
        walletId: String,
        assetId: String,
        chain: Chain?,
        state: List<BannerState> = listOf(BannerState.Active, BannerState.AlwaysActive)
    ): List<DbBanner>


    @Query("""
        SELECT state FROM
            banners
        WHERE
            wallet_id=:walletId AND event = "AccountBlockedMultiSignature"
    """)
    fun getMultisign(walletId: String): Flow<List<BannerState>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBanner(banner: DbBanner)
}