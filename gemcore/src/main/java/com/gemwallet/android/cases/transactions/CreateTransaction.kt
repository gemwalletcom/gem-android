package com.gemwallet.android.cases.transactions

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.Transaction
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface CreateTransaction {
    suspend fun createTransaction(
        hash: String,
        walletId: String,
        assetId: AssetId,
        owner: Account,
        to: String,
        state: TransactionState,
        fee: Fee,
        amount: BigInteger,
        memo: String?,
        type: TransactionType,
        metadata: String? = null,
        direction: TransactionDirection,
        blockNumber: String,
    ): Transaction
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Long::class)
object DateSerializer {

    override fun deserialize(decoder: Decoder): Long {
        return ZonedDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME)
            .toInstant()
            .toEpochMilli()

    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeString(value.toString())
    }
}