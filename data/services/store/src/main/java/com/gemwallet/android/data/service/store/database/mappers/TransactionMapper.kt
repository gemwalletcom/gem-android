package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbTransaction
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Transaction

class TransactionMapper() : Mapper<Transaction, DbTransaction, String, Nothing> {
    override fun asDomain(entity: Transaction, options: (() -> String)?): DbTransaction = DbTransaction(
        id = entity.id,
        walletId = options?.invoke() ?: throw IllegalArgumentException(),
        hash = entity.hash,
        assetId = entity.assetId.toIdentifier(),
        feeAssetId = entity.feeAssetId.toIdentifier(),
        owner = entity.from,
        recipient = entity.to,
        contract = entity.contract,
        type = entity.type,
        state = entity.state,
        blockNumber = entity.blockNumber,
        sequence = entity.sequence,
        fee = entity.fee,
        value = entity.value,
        payload = entity.memo,
        metadata = entity.metadata,
        direction = entity.direction,
        updatedAt = System.currentTimeMillis(),
        createdAt = entity.createdAt,
    )

    override fun asEntity(domain: DbTransaction, options: (() -> Nothing)?): Transaction = Transaction(
        id = domain.id,
        hash = domain.hash,
        assetId = domain.assetId.toAssetId() ?: throw IllegalArgumentException(),
        from = domain.owner,
        to = domain.recipient,
        contract = domain.contract,
        type = domain.type,
        state = domain.state,
        blockNumber = domain.blockNumber,
        sequence = domain.sequence,
        fee = domain.fee,
        feeAssetId = domain.feeAssetId.toAssetId() ?: throw IllegalArgumentException(),
        value = domain.value,
        memo = domain.payload,
        direction = domain.direction,
        utxoInputs = emptyList(),
        utxoOutputs = emptyList(),
        createdAt = if (domain.createdAt == 0L) System.currentTimeMillis() else domain.createdAt,
        metadata = domain.metadata,
    )
}