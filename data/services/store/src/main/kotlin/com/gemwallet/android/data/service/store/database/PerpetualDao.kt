package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbPerpetual
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualData
import kotlinx.coroutines.flow.Flow

@Dao
interface PerpetualDao {
    @Insert
    suspend fun putPerpetuals(items: List<DbPerpetual>)

    @Delete
    suspend fun deletePerpetuals(items: List<DbPerpetual>)

    @Query("SELECT * FROM perpetual")
    fun getPerpetuals(): Flow<List<DbPerpetual>>

    @Query("""
        SELECT * FROM perpetual
        JOIN perpetual_metadata ON perpetual_metadata.id = id
        JOIN asset ON asset.id = assetId
    """)
    fun getPerpetualsData(): Flow<List<DbPerpetualData>>

    @Query("""
        SELECT * FROM perpetual
        JOIN perpetual_metadata ON perpetual_metadata.id = id
        JOIN asset ON asset.id = assetId
        WHERE id = :perpetualId
    """)
    fun getPerpetual(perpetualId: String): Flow<DbPerpetualData>
}