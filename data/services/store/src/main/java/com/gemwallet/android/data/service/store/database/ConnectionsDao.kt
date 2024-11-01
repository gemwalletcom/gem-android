package com.gemwallet.android.data.service.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemwallet.android.data.service.store.database.entities.DbConnection

@Dao
interface ConnectionsDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(connection: DbConnection)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(connections: List<DbConnection>)

    @Update
    suspend fun update(connection: DbConnection)

    @Query("SELECT * FROM room_connection")
    suspend fun getAll(): List<DbConnection>

    @Query("SELECT * FROM room_connection WHERE session_id = :sessionId")
    suspend fun getBySessionId(sessionId: String): DbConnection

    @Query("DELETE FROM room_connection WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM room_connection")
    suspend fun deleteAll()
}