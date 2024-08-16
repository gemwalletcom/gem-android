package com.gemwallet.android.data.config

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.gemwallet.android.ext.findByString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "nodes")
data class RoomNode(
    @PrimaryKey val url: String,
    val status: NodeState,
    val priority: Int,
    val chain: Chain,
)

@Dao
interface NodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNodes(nodes: List<RoomNode>)

    @Delete
    suspend fun deleteNode(node: List<RoomNode>)

    @Query("SELECT * FROM nodes")
    fun getNodes(): Flow<List<RoomNode>>

}

class NodeLocalSource(private val dao: NodeDao) {

    suspend fun addNodes(nodes: List<ChainNodes>) {
        dao.addNodes(nodes.toEntity())
    }

    suspend fun delete(node: ChainNodes) {
        dao.deleteNode(node.toEntity())
    }

    fun getNodes(): Flow<List<ChainNodes>> {
        return dao.getNodes().map { it.toDomain() }
    }
}

private fun ChainNodes.toEntity(): List<RoomNode> {
    return nodes.mapNotNull {
        RoomNode(
            chain = Chain.findByString(chain) ?: return@mapNotNull null,
            url = it.url,
            status = it.status,
            priority = it.priority,
        )
    }
}

private fun List<ChainNodes>.toEntity(): List<RoomNode> = map { it.toEntity() }.flatten()

private fun List<RoomNode>.toDomain() = groupBy { it.chain }.map { entry ->
    ChainNodes(
        chain = entry.key.string,
        nodes = entry.value.map {
            Node(
                url = it.url,
                priority = it.priority,
                status = it.status,
            )
        }
    )
}