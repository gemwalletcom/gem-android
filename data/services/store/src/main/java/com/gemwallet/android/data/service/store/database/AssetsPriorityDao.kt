package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbAssetPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetsPriorityDao {

    @Insert(onConflict = REPLACE)
    suspend fun put(priorities: List<DbAssetPriority>)

    @Query("""
        SELECT COUNT(asset_id) FROM assets_priority WHERE `query` = :query
    """)
    fun hasPriorities(query: String): Flow<Int>
}