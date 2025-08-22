package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinBroadcastService
import com.gemwallet.android.math.decodeHex
import com.wallet.core.blockchain.bitcoin.BitcoinTransactionBroacastResult
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Test

class TestBitcoinBroadcast {

    val sign = "010000000153bd8a94cf6424d8e54cacbad6082c249cdacdd0956f766206a" +
            "c3daed5e5479f010000006b483045022100ead2b7637532b167e66ddf76eafd032ada898c8719a" +
            "680de8183b71fb3086a3e022055a073be9148cb8b9dc71de04755dbf11f140fee167ed0d4b44d6" +
            "a54a829e75e012102fd6585adc0e86019abf00e83552d054cb5f4359ad4db8ca338099381f43e2" +
            "5a5000000000182a82005000000001976a91424849c1d94eb9e6e002dd75fdcbce0a9673daba78" +
            "8ac00000000"

    @Test
    fun testBitcoinBroadcast() {
        var sendingData: String = ""
        val broadcastClient = BitcoinBroadcastClient(
            Chain.Bitcoin,
            object : BitcoinBroadcastService {
                override suspend fun broadcast(request: RequestBody): Result<BitcoinTransactionBroacastResult> {
                    assertEquals(Mime.Plain.value, request.contentType())
                    val buffer = Buffer()
                    request.writeTo(buffer)
                    sendingData = String(buffer.inputStream().readAllBytes())
                    return Result.success(
                        BitcoinTransactionBroacastResult(result = "some hash")
                    )
                }
            }

        )
        val result = runBlocking {
            broadcastClient.send(
                Account(Chain.Bitcoin, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                sign.decodeHex(),
                TransactionType.Transfer,
            )
        }

        assertEquals(sign, sendingData)
        assertEquals("some hash", result)
    }
}