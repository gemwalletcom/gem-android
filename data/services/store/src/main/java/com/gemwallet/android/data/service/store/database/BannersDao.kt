package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain

@Dao
interface BannersDao {
    @Query("SELECT * FROM banners WHERE wallet_id=:walletId AND (asset_id=:assetId OR asset_id LIKE :chain || '%') AND event = :event")
    suspend fun getBanner(walletId: String?, assetId: String?, chain: String?, event: BannerEvent): DbBanner?

    @Query("""
        SELECT * FROM
            banners
        WHERE
            wallet_id=:walletId AND (asset_id=:assetId OR chain=:chain) AND state=:state
    """)
    suspend fun getBanner(walletId: String, assetId: String, chain: Chain?, state: BannerState = BannerState.Active): List<DbBanner>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBanner(banner: DbBanner)
}