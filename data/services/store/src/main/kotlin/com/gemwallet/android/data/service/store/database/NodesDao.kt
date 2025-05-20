package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gemwallet.android.data.service.store.database.entities.DbNode
import kotlinx.coroutines.flow.Flow

@Dao
interface NodesDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addNodes(nodes: List<DbNode>)

    @Delete
    suspend fun deleteNode(node: List<DbNode>)

    @Query("SELECT * FROM nodes")
    fun getNodes(): Flow<List<DbNode>>

}