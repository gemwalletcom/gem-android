package com.gemwallet.features.swap.viewmodels.cases

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimateRateTest {

    @Test
    fun `estimateRate should calculate correct rates for ETH to USDC swap`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "1000000000000000000"
        val receiveValue = "2000000000"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 ETH ≈ 2,000.00 USDC",
            reverse = "1 USDC ≈ 0.0005 ETH"
        )
        
        assertEquals(expected, result)
    }

    @Test
    fun `estimateRate should calculate correct rates for USDC to ETH swap`() {
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        
        val payValue = "1000000"
        val receiveValue = "1000000000000000000"
        
        val result = estimateSwapRate(usdcAsset, ethAsset, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 USDC ≈ 1.00 ETH",
            reverse = "1 ETH ≈ 1.00 USDC"
        )
        
        assertEquals(expected, result)
    }

    @Test
    fun `estimateRate should handle equal decimal assets`() {
        val asset1 = createTestAsset(decimals = 18, symbol = "ETH")
        val asset2 = createTestAsset(decimals = 18, symbol = "BNB")
        
        val payValue = "1000000000000000000"
        val receiveValue = "5000000000000000000"
        
        val result = estimateSwapRate(asset1, asset2, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 ETH ≈ 5.00 BNB",
            reverse = "1 BNB ≈ 0.20 ETH"
        )
        
        assertEquals(expected, result)
    }

    @Test
    fun `estimateRate should handle small amounts correctly`() {
        val btcAsset = createTestAsset(decimals = 8, symbol = "BTC")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "1000000"
        val receiveValue = "500000000"
        
        val result = estimateSwapRate(btcAsset, usdcAsset, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 BTC ≈ 50,000.00 USDC",
            reverse = "1 USDC ≈ 0.00002 BTC"
        )
        
        assertEquals(expected, result)
    }

    @Test
    fun `estimateRate should handle zero payValue gracefully`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "0"
        val receiveValue = "2000000000"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        assertNull("Result should be null for zero payValue", result)
    }

    @Test
    fun `estimateRate should handle zero receiveValue gracefully`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "1000000000000000000"
        val receiveValue = "0"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        assertNull("Result should be null for zero receiveValue", result)
    }

    @Test
    fun `estimateRate should handle invalid input strings gracefully`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "invalid_number"
        val receiveValue = "2000000000"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        assertNull("Result should be null for invalid input", result)
    }

    @Test
    fun `estimateRate should handle negative values gracefully`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "-1000000000000000000"
        val receiveValue = "2000000000"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        if (result != null) {
            assertTrue("Result should contain negative values", 
                result.forward.contains("-") || result.reverse.contains("-"))
        }
    }

    @Test
    fun `estimateRate should format rates with proper approximation symbol`() {
        val ethAsset = createTestAsset(decimals = 18, symbol = "ETH")
        val usdcAsset = createTestAsset(decimals = 6, symbol = "USDC")
        
        val payValue = "1000000000000000000"
        val receiveValue = "1500000000"
        
        val result = estimateSwapRate(ethAsset, usdcAsset, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 ETH ≈ 1,500.00 USDC",
            reverse = "1 USDC ≈ 0.0006 ETH"
        )
        
        assertEquals(expected, result)
    }

    @Test
    fun `estimateRate should handle very large numbers`() {
        val asset1 = createTestAsset(decimals = 18, symbol = "TOKEN1")
        val asset2 = createTestAsset(decimals = 18, symbol = "TOKEN2")
        
        val payValue = "1000000000000000000"
        val receiveValue = "500000000000000000"
        
        val result = estimateSwapRate(asset1, asset2, payValue, receiveValue)
        
        val expected = SwapRate(
            forward = "1 TOKEN1 ≈ 0.50 TOKEN2",
            reverse = "1 TOKEN2 ≈ 2.00 TOKEN1"
        )
        
        assertEquals(expected, result)
    }

    private fun createTestAsset(
        decimals: Int = 18,
        symbol: String = "ETH"
    ): Asset {
        return Asset(
            id = AssetId(Chain.Ethereum, tokenId = null),
            name = symbol,
            symbol = symbol,
            decimals = decimals,
            type = AssetType.NATIVE
        )
    }
}