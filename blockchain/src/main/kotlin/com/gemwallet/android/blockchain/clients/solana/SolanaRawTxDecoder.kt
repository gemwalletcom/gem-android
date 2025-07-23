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

    fun signatures(): ByteArray {
        var offset = 0
        val count = decodeShortVecLength(offset = offset)

        var result: ByteArray = byteArrayOf()
        for (i in 0 ..< count) {
            val sig = rawData.sliceArray(offset ..< (offset + 64))
            result += sig
            offset += 64
        }
        return result
    }

    fun messageData(): List<Byte> {
        val sigCount = decodeShortVecLength(offset = 0).toInt()
        val offset = 1 + sigCount * 64
        return rawData.slice(offset ..< rawData.size)
    }
}