package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.aptos.services.AptosBroadcastService
import com.gemwallet.android.math.decodeHex
import com.wallet.core.blockchain.aptos.models.AptosTransactionBroacast
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Test

class TestAptosBroadcast {

    val sign: ByteArray = ("0x7b2265787069726174696f6e5f74696d657374616d705f73656373223a223336363" +
            "4333930303832222c226761735f756e69745f7072696365223a22313530222c226d61785f6761735f616" +
            "d6f756e74223a223138222c227061796c6f6164223a7b22617267756d656e7473223a5b2230783832313" +
            "131663239373561306636303830643137383233363336396237343739663661656431323033656634613" +
            "2336638323035653462393137313662373833222c223130303030303030303030225d2c2266756e63746" +
            "96f6e223a223078313a3a6170746f735f6163636f756e743a3a7472616e73666572222c2274797065223" +
            "a22656e7472795f66756e6374696f6e5f7061796c6f6164222c22747970655f617267756d656e7473223" +
            "a5b5d7d2c2273656e646572223a223078396231646238313138306333316231623432383537326265313" +
            "03565323039623561363232326237222c2273657175656e63655f6e756d626572223a2238222c2273696" +
            "76e6174757265223a7b227075626c69635f6b6579223a223078633163343364356164646665316332333" +
            "761646164363937326435663338663762393631353661636663366637656664386662346435333037653" +
            "06365383861222c227369676e6174757265223a223078663063666439373630396263656639636661646" +
            "239306264386566363565313432643862393039666537306137383539393434323330656234306566353" +
            "061623236306130623931663935633632326364636537636637313835323365356565623532366433666" +
            "1356438393635646431333966333637303930653562303031222c2274797065223a22656432353531395" +
            "f7369676e6174757265227d7d").decodeHex()
    val actualSendingData = "{" +
            "\"expiration_timestamp_secs\":\"3664390082\"," +
            "\"gas_unit_price\":\"150\"," +
            "\"max_gas_amount\":\"18\"," +
            "\"payload\":{" +
                "\"arguments\":[\"0x82111f2975a0f6080d178236369b7479f6aed1203ef4a23f8205e4b91716b783\",\"10000000000\"]," +
                "\"function\":\"0x1::aptos_account::transfer\",\"type\":\"entry_function_payload\",\"type_arguments\":[]}," +
            "\"sender\":\"0x9b1db81180c31b1b428572be105e209b5a6222b7\"," +
            "\"sequence_number\":\"8\"," +
            "\"signature\":{" +
                "\"public_key\":\"0xc1c43d5addfe1c237adad6972d5f38f7b96156acfc6f7efd8fb4d5307e0ce88a\"," +
                "\"signature\":\"0xf0cfd97609bcef9cfadb90bd8ef65e142d8b909fe70a7859944230eb40ef50ab260a0b91f95c622cdce7cf718523e5eeb526d3fa5d8965dd139f367090e5b001\"," +
                "\"type\":\"ed25519_signature\"}}"

    @Test
    fun testAptosBroadcast() {
        var sendingData: String = ""
        val broadcastClient = AptosBroadcastClient(
            Chain.Aptos,
            object : AptosBroadcastService {
                override suspend fun broadcast(request: RequestBody): Result<AptosTransactionBroacast> {
                    assertEquals(Mime.Json.value, request.contentType())
                    val buffer = Buffer()
                    request.writeTo(buffer)
                    sendingData = String(buffer.inputStream().readAllBytes())
                    return Result.success(
                        AptosTransactionBroacast(hash = "some hash")
                    )
                }
            }

        )
        val result = runBlocking {
            broadcastClient.send(
                Account(Chain.Aptos, "0x9b1DB81180c31B1b428572Be105E209b5A6222b7", ""),
                sign,
                TransactionType.Transfer,
            )
        }

        assertEquals(actualSendingData, sendingData)
        assertEquals("some hash", result)
    }
}