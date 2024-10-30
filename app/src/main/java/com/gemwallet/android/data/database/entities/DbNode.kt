package com.gemwallet.android.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NodeState

@Entity(tableName = "nodes")
data class DbNode(
    @PrimaryKey val url: String,
    val status: NodeState,
    val priority: Int,
    val chain: Chain,
)