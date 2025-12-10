package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualPosition
import com.gemwallet.android.data.service.store.database.entities.DbPerpetualPositionData
import kotlinx.coroutines.flow.Flow

@Dao
interface PerpetualPositionDao {

    @Insert(onConflict = REPLACE)
    suspend fun putPositions(items: List<DbPerpetualPosition>)

    @Delete
    suspend fun deletePositions(items: List<DbPerpetualPosition>)

    @Query("SELECT * FROM perpetual_position WHERE accountAddress IN (:accountAddress)")
    fun getPositions(accountAddress: List<String>): Flow<List<DbPerpetualPosition>>

    @Transaction
    @Query("""SELECT * FROM perpetual_position WHERE accountAddress IN (:accountAddresses)""")
    fun getPositionsData(accountAddresses: List<String>): Flow<List<DbPerpetualPositionData>>

    @Transaction
    @Query("""
        SELECT * FROM perpetual_position
        WHERE perpetual_position.id = :positionId
    """)
    fun getPositionData(positionId: String): Flow<DbPerpetualPositionData?>

}