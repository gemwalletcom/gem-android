package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.database.entities.DbBanner
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.BannerEvent

@Dao
interface BannersDao {
    @Query("SELECT * FROM banners WHERE wallet_id=:walletId AND asset_id=:assetId AND event = :event")
    suspend fun getBanner(walletId: String?, assetId: String?, event: BannerEvent): DbBanner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBanner(banner: DbBanner)
}