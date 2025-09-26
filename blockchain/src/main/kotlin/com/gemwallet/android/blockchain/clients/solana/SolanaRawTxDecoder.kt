package com.gemwallet.android.blockchain.clients.solana

import kotlin.experimental.and

class SolanaRawTxDecoder(
    val rawData: ByteArray
) {
    fun decodeShortVecLength(offset: Int): Byte {
        val byte = rawData[offset]
        return byte and 0x7f
    }

    fun signatureCount(): Byte {
        return decodeShortVecLength(offset = 0)
    }
}