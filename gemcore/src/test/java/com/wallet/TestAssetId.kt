package com.wallet

import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Test

class TestAssetId {
    @Test
    fun testTonAssetId() {
        val assetId = "ton_EQAvlWFDxGF2lXm67y4yzC17wYKD9A0guwPkMs1gOsM__NOT".toAssetId()!!
        assertEquals(assetId.chain, Chain.Ton)
        assertEquals(assetId.tokenId, "EQAvlWFDxGF2lXm67y4yzC17wYKD9A0guwPkMs1gOsM__NOT")
    }
}