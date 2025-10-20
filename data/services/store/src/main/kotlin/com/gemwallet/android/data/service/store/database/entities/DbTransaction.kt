package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import com.gemwallet.android.ext.hash
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Transaction
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

fun Transaction.toRecord(walletId: String): DbTransaction {
    return DbTransaction(
        id = this.id,
        walletId = walletId,
        hash = this.hash,
        assetId = this.assetId.toIdentifier(),
        feeAssetId = this.feeAssetId.toIdentifier(),
        owner = this.from,
        recipient = this.to,
        contract = this.contract,
        type = this.type,
        state = this.state,
        blockNumber = this.blockNumber ?: "",
        sequence = this.sequence ?: "",
        fee = this.fee,
        value = this.value,
        payload = this.memo,
        metadata = this.metadata,
        direction = this.direction,
        updatedAt = System.currentTimeMillis(),
        createdAt = this.createdAt,
    )
}

fun DbTransaction.toModel(): Transaction {
    return Transaction(
        id = this.id,
        assetId = this.assetId.toAssetId() ?: throw IllegalArgumentException(),
        from = this.owner,
        to = this.recipient,
        contract = this.contract,
        type = this.type,
        state = this.state,
        blockNumber = this.blockNumber,
        sequence = this.sequence,
        fee = this.fee,
        feeAssetId = this.feeAssetId.toAssetId() ?: throw IllegalArgumentException(),
        value = this.value,
        memo = this.payload,
        direction = this.direction,
        utxoInputs = emptyList(),
        utxoOutputs = emptyList(),
        createdAt = if (this.createdAt == 0L) System.currentTimeMillis() else this.createdAt,
        metadata = this.metadata,
    )
}

fun List<DbTransaction>.toModel() = map { it.toModel() }

fun List<Transaction>.toRecord(walletId: String) = map { it.toRecord(walletId) }