package com.gemwallet.android.blockchain.operators

import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

interface SignTransfer {
    suspend operator fun invoke(
        input: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): Result<ByteArray>

    suspend operator fun invoke(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray,
        isTyped: Boolean = false,
    ): Result<ByteArray>
}