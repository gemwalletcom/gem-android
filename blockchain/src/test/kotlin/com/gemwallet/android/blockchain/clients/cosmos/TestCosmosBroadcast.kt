package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosBroadcastService
import com.gemwallet.android.math.decodeHex
import com.wallet.core.blockchain.cosmos.models.CosmosBroadcastResponse
import com.wallet.core.blockchain.cosmos.models.CosmosBroadcastResult
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Test

class TestCosmosBroadcast {

    val sign = "0x7b226d6f6465223a2242524f4144434153545f4d4f44455f53594e43222c2274785f6279746573223" +
            "a22436f6f42436f6342436877765932397a6257397a4c6d4a68626d7375646a46695a585268" +
            "4d53354e633264545a57356b456d634b4b32397a62573878613264735a57313162585534625" +
            "734324e5468714e6d6330656a6c71656d347a656d566d4d6e466b65586c326132783359544d" +
            "534b32397a62573878636d4e71646e70364f486436613352785a6e6f346357706d4d4777356" +
            "354513161337034646d5177656a42754e3277315932596143776f466457397a62573853416a" +
            "4577456d674b55417047436838765932397a6257397a4c6d4e79655842306279357a5a574e7" +
            "74d6a5532617a4575554856695332563545694d4b49514d736c63596e374468506535622f38" +
            "6c4d33466e50586847426a3553644331352b584931685a31675962424249454367494941526" +
            "74b4568514b44676f466457397a62573853425445774d444177454d436144427041564a6b44" +
            "786153355a6167686d4a365a7470433979696d374a413864754f384d774f4f44644a6548454" +
            "87373483350514e2b34596c2b5356794c744e4557362b4944554b666b4731646649594f7670" +
            "5269466c4f79673d3d227d"

    @Test
    fun testCosmosBroadcast() {
        var sendingData: String = ""
        val broadcastClient = CosmosBroadcastClient(
            Chain.Cosmos,
            object : CosmosBroadcastService {
                override suspend fun broadcast(request: RequestBody): Result<CosmosBroadcastResponse> {
                    assertEquals(Mime.Json.value, request.contentType())
                    val buffer = Buffer()
                    request.writeTo(buffer)
                    sendingData = String(buffer.inputStream().readAllBytes())
                    return Result.success(
                        CosmosBroadcastResponse(CosmosBroadcastResult(txhash = "some hash", code = 0, raw_log = ""))
                    )
                }
            }

        )
        val result = runBlocking {
            broadcastClient.send(
                Account(Chain.Cosmos, "cosmos1kglemumu8mn658j6g4z9jzn3zef2qdyyydv7tr", ""),
                sign.decodeHex(),
                TransactionType.Transfer,
            )
        }

        assertEquals(
            "{\"mode\":\"BROADCAST_MODE_SYNC\",\"tx_bytes\":\"CooBCocBChwvY29zbW9zLmJhb" +
                "msudjFiZXRhMS5Nc2dTZW5kEmcKK29zbW8xa2dsZW11bXU4bW42NThqNmc0ejlqem4zemVmMnFkeXl2" +
                "a2x3YTMSK29zbW8xcmNqdnp6OHd6a3RxZno4cWpmMGw5cTQ1a3p4dmQwejBuN2w1Y2YaCwoFdW9zbW8" +
                "SAjEwEmgKUApGCh8vY29zbW9zLmNyeXB0by5zZWNwMjU2azEuUHViS2V5EiMKIQMslcYn7DhPe5b/8l" +
                "M3FnPXhGBj5SdC15+XI1hZ1gYbBBIECgIIARgKEhQKDgoFdW9zbW8SBTEwMDAwEMCaDBpAVJkDxaS5Z" +
                "aghmJ6ZtpC9yim7JA8duO8MwOODdJeHEHssH3PQN+4Yl+SVyLtNEW6+IDUKfkG1dfIYOvpRiFlOyg==\"}",
            sendingData
        )
        assertEquals("some hash", result.getOrNull())
    }

    @Test
    fun testCosmosBroadcastFail() {
        var sendingData: String = ""
        val broadcastClient = CosmosBroadcastClient(
            Chain.Cosmos,
            object : CosmosBroadcastService {
                override suspend fun broadcast(request: RequestBody): Result<CosmosBroadcastResponse> {
                    assertEquals(Mime.Json.value, request.contentType())
                    val buffer = Buffer()
                    request.writeTo(buffer)
                    sendingData = String(buffer.inputStream().readAllBytes())
                    return Result.success(
                        CosmosBroadcastResponse(CosmosBroadcastResult(txhash = "some hash", code = 1, raw_log = "Some error"))
                    )
                }
            }

        )
        val result = runBlocking {
            broadcastClient.send(
                Account(Chain.Cosmos, "cosmos1kglemumu8mn658j6g4z9jzn3zef2qdyyydv7tr", ""),
                sign.decodeHex(),
                TransactionType.Transfer,
            )
        }

        assertEquals("Some error", result.exceptionOrNull()?.message)
    }
}