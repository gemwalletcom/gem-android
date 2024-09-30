package com.gemwallet.android.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

@Entity(tableName = "transactions", primaryKeys = ["id", "walletId"])
data class DbTransaction(
    val id: String,
    val walletId: String,
    val hash: String,
    val assetId: String,
    val feeAssetId: String,
    val owner: String,
    val recipient: String,
    val contract: String? = null,
    val metadata: String? = null,
    val state: TransactionState,
    val type: TransactionType,
    val blockNumber: String,
    val sequence: String,
    val fee: String, // Atomic value - BigInteger
    val value: String, // Atomic value - BigInteger
    val payload: String? = null,
    val direction: TransactionDirection,
    val createdAt: Long,
    val updatedAt: Long,
)