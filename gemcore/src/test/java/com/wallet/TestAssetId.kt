package com.wallet

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TestAssetId {
    @Test
    fun testTonAssetId() {
        val assetId = "ton_EQAvlWFDxGF2lXm67y4yzC17wYKD9A0guwPkMs1gOsM__NOT".toAssetId()!!
        assertEquals(assetId.chain, Chain.Ton)
        assertEquals(assetId.tokenId, "EQAvlWFDxGF2lXm67y4yzC17wYKD9A0guwPkMs1gOsM__NOT")
    }

    @Test
    fun testAssetIdDeserializer() {
        assertEquals(AssetId(Chain.Ethereum), jsonEncoder.decodeFromString<AssetId>("ethereum"))
        assertEquals(AssetId(Chain.Ethereum, "0xABSDEEF"), jsonEncoder.decodeFromString<AssetId>("ethereum_0xABSDEEF"))
        assertEquals(
            AssetId(Chain.Ethereum),
            jsonEncoder.decodeFromString<AssetId>("""
                {
                  "chain": "Ethereum"
                }
            """.trimIndent())
        )
        assertEquals(
            AssetId(Chain.Ethereum, "0xABSDEEF"),
            jsonEncoder.decodeFromString<AssetId>("""
                {
                  "chain": "Ethereum",
                  "tokenId": "0xABSDEEF"
                }
            """.trimIndent())
        )
        try {
            jsonEncoder.decodeFromString<AssetId>("""
                {
                  "chain": "FooChain",
                  "tokenId": "0xABSDEEF"
                }
            """.trimIndent())
            assertTrue(false)
        } catch (err: Throwable) {
            assertTrue(err is IOException)
        }
        assertEquals("ethereum", jsonEncoder.encodeToString(AssetId(Chain.Ethereum)))
        assertEquals("ethereum_SomeTOken", jsonEncoder.encodeToString(AssetId(Chain.Ethereum, "SomeTOken")))
    }
}