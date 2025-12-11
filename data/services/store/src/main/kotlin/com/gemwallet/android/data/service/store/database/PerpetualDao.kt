package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.gemwallet.android.data.service.store.database.entities.DbPerpetual
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualAsset
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualData
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface PerpetualDao {
    @Insert(onConflict = REPLACE)
    suspend fun putPerpetuals(items: List<DbPerpetual>)

    @Insert(onConflict = REPLACE)
    suspend fun putAssets(items: List<DbPerpetualAsset>)

    @Transaction
    suspend fun putPerpetualsData(perpetuals: List<DbPerpetual>, assets: List<DbPerpetualAsset>) {
        putPerpetuals(perpetuals)
        putAssets(assets)
    }

    @Delete
    suspend fun deletePerpetuals(items: List<DbPerpetual>)

    @Query("SELECT * FROM perpetual")
    fun getPerpetuals(): Flow<List<DbPerpetual>>

    @Transaction
    @Query("""
        SELECT * FROM perpetual
        WHERE volume24h > 0
        ORDER BY volume24h DESC
    """)
    fun getPerpetualsData(): Flow<List<DbPerpetualData>>

    @Transaction
    @Query("""
            SELECT * FROM perpetual
            WHERE id = :perpetualId
    """)
    fun getPerpetual(perpetualId: String): Flow<DbPerpetualData?>

    @Insert(onConflict = REPLACE)
    fun setMetadata(metadata: DbPerpetualMetadata)
}