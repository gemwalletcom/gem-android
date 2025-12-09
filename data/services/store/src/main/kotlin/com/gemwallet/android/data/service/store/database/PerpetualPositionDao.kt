package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualPosition
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualPositionData
import kotlinx.coroutines.flow.Flow

@Dao
interface PerpetualPositionDao {

    @Insert
    suspend fun putPositions(items: List<DbPerpetualPosition>)

    @Delete
    suspend fun deletePositions(items: List<DbPerpetualPosition>)

    @Query("SELECT * FROM perpetual_position WHERE accountAddress IN (:accountAddress)")
    fun getPositions(accountAddress: List<String>): Flow<List<DbPerpetualPosition>>

    @Query("""
        SELECT * FROM perpetual_position
        JOIN perpetual ON perpetualId = perpetual.id
        JOIN asset ON assetId = asset.id
        WHERE accountAddress IN (:accountAddresses)
    """)
    fun getPositionsData(accountAddresses: List<String>): Flow<List<DbPerpetualPositionData>>

    @Query("""
        SELECT * FROM perpetual_position
        JOIN perpetual ON perpetualId = perpetual.id
        JOIN asset ON assetId = asset.id
        WHERE perpetual_position.id = :positionId
    """)
    fun getPositionData(positionId: String): Flow<DbPerpetualPositionData?>

}