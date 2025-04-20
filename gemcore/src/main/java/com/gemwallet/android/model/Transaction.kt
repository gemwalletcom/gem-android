package com.gemwallet.android.model

import com.gemwallet.android.serializer.DateSerializer
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionInput
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class Transaction (
    val id: String,
    val hash: String,
    val assetId: AssetId,
    val from: String,
    val to: String,
    val contract: String? = null,
    val type: TransactionType,
    val state: TransactionState,
    val blockNumber: String,
    val sequence: String,
    val fee: String,
    val feeAssetId: AssetId,
    val value: String,
    val memo: String? = null,
    val direction: TransactionDirection,
    val utxoInputs: List<TransactionInput>,
    val utxoOutputs: List<TransactionInput>,
    val metadata: String? = null,
    @Serializable(with = DateSerializer::class)
    val createdAt: Long,
)

