package com.gemwallet.android.data.repositoreis.bridge

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChainNamespaceAndroidTest {

    @Test
    fun getNamespace_withNullInput_returnsNull() {
        val result = Chain.getNamespace(null)
        assertNull(result)
    }

    @Test
    fun getNamespace_withEmptyString_returnsNull() {
        val result = Chain.getNamespace("")
        assertNull(result)
    }

    @Test
    fun getNamespace_withInvalidFormat_noColon_returnsNull() {
        val result = Chain.getNamespace("invalid_format")
        assertNull(result)
    }

    @Test
    fun getNamespace_withInvalidFormat_onlyOneColon_returnsNull() {
        val result = Chain.getNamespace("eip155:")
        assertNull(result)
    }

    @Test
    fun getNamespace_withInvalidFormat_emptyNamespace_returnsNull() {
        val result = Chain.getNamespace(":123")
        assertNull(result)
    }

    @Test
    fun getNamespace_withInvalidFormat_singleElement_returnsNull() {
        val result = Chain.getNamespace("eip155")
        assertNull(result)
    }

    @Test
    fun getNamespace_withValidEthereumMainnet_returnsEthereumChain() {
        val result = Chain.getNamespace("eip155:1")
        assertEquals(Chain.Ethereum, result)
    }

    @Test
    fun getNamespace_withValidBSCMainnet_returnsSmartChain() {
        val result = Chain.getNamespace("eip155:56")
        assertEquals(Chain.SmartChain, result)
    }

    @Test
    fun getNamespace_withValidPolygonMainnet_returnsPolygon() {
        val result = Chain.getNamespace("eip155:137")
        assertEquals(Chain.Polygon, result)
    }

    @Test
    fun getNamespace_withValidArbitrumOne_returnsArbitrum() {
        val result = Chain.getNamespace("eip155:42161")
        assertEquals(Chain.Arbitrum, result)
    }

    @Test
    fun getNamespace_withValidOptimismMainnet_returnsOptimism() {
        val result = Chain.getNamespace("eip155:10")
        assertEquals(Chain.Optimism, result)
    }

    @Test
    fun getNamespace_withValidAvalancheC_returnsAvalancheC() {
        val result = Chain.getNamespace("eip155:43114")
        assertEquals(Chain.AvalancheC, result)
    }

    @Test
    fun getNamespace_withValidBaseMainnet_returnsBase() {
        val result = Chain.getNamespace("eip155:8453")
        assertEquals(Chain.Base, result)
    }

    @Test
    fun getNamespace_withValidSolanaMainnet_returnsSolana() {
        val result = Chain.getNamespace("solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp")
        assertEquals(Chain.Solana, result)
    }

    @Test
    fun getNamespace_withUnsupportedBitcoinMainnet_returnsNull() {
        val result = Chain.getNamespace("bip122:000000000019d6689c085ae165831e93")
        assertNull(result)
    }

    @Test
    fun getNamespace_withMultipleColons_usesFirstTwoParts() {
        val result = Chain.getNamespace("eip155:1:extra:data")
        assertEquals(Chain.Ethereum, result)
    }

    @Test
    fun getNamespace_withUnknownChainId_returnsNull() {
        val result = Chain.getNamespace("eip155:99999")
        assertNull(result)
    }

    @Test
    fun getNamespace_withUnknownNamespace_returnsNull() {
        val result = Chain.getNamespace("unknown:123")
        assertNull(result)
    }

    @Test
    fun getNamespace_withWhitespaceAroundInput_returnsNull() {
        val result = Chain.getNamespace(" eip155:1 ")
        assertNull(result)
    }

    @Test
    fun getNamespace_withValidFantomMainnet_returnsFantom() {
        val result = Chain.getNamespace("eip155:250")
        assertEquals(Chain.Fantom, result)
    }

    @Test
    fun getNamespace_withUnsupportedTonMainnet_returnsNull() {
        val result = Chain.getNamespace("ton:-239")
        assertNull(result)
    }

    @Test
    fun getNamespace_withUnsupportedTronMainnet_returnsNull() {
        val result = Chain.getNamespace("tron:0x2b6653dc")
        assertNull(result)
    }

    @Test
    fun getNamespace_withValidCosmosHub_returnsCosmos() {
        val result = Chain.getNamespace("cosmos:cosmoshub-4")
        assertEquals(Chain.Cosmos, result)
    }

    @Test
    fun getNamespace_withValidOsmosis_returnsOsmosis() {
        val result = Chain.getNamespace("cosmos:osmosis-1")
        assertEquals(Chain.Osmosis, result)
    }
}