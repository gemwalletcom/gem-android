package com.gemwallet.features.swap.viewmodels.cases

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uniffi.gemstone.SwapperProviderData
import uniffi.gemstone.SwapperProviderType
import uniffi.gemstone.SwapperQuote
import uniffi.gemstone.SwapperQuoteRequest

class GetProvidersTest {

    @Test
    fun `getProviders should return empty list when no quotes provided`() {
        val result = getProviders(
            items = emptyList(),
            priceValue = 50000.0,
            currency = Currency.USD,
            asset = createTestAsset()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getProviders should handle single quote correctly`() {
        val mockProviderType = mockk<SwapperProviderType> {
            every { protocol } returns "UniswapV3"
            every { name } returns "Uniswap V3"
        }
        
        val mockProviderData = mockk<SwapperProviderData> {
            every { provider } returns mockProviderType
        }

        val mockQuoteRequest = mockk<SwapperQuoteRequest>()
        
        val mockQuote = mockk<SwapperQuote> {
            every { toValue } returns "2000000000000000000" // 2.0 ETH in wei
            every { data } returns mockProviderData
            every { request } returns mockQuoteRequest
            every { fromValue } returns "1000000000000000000" // 1.0 ETH
            every { etaInSeconds } returns null
        }

        val result = getProviders(
            items = listOf(mockQuote),
            priceValue = 3000.0, // $3000 per ETH
            currency = Currency.USD,
            asset = createTestAsset(decimals = 18, symbol = "ETH")
        )

        assertEquals(1, result.size)
        val providerItem = result.first()
        
        assertNotNull(providerItem.swapProvider)
        assertEquals("UniswapV3", providerItem.swapProvider.protocol)
        
        // Verify the price contains "2" (the amount)
        assertNotNull("Price should not be null", providerItem.price)
        assertTrue("Price should contain '2', got: '${providerItem.price}'", 
            providerItem.price?.contains("2") == true)
        
        // Verify the fiat value is calculated correctly (2.0 ETH * $3000 = $6000)
        assertNotNull("Fiat should not be null", providerItem.fiat)
        assertTrue("Fiat should contain '6,000', got: '${providerItem.fiat}'", 
            providerItem.fiat?.contains("6,000") == true)
    }

    @Test
    fun `getProviders should handle multiple quotes with different providers`() {
        val uniswapProvider = mockk<SwapperProviderType> {
            every { protocol } returns "UniswapV3"
            every { name } returns "Uniswap V3"
        }
        
        val sushiProvider = mockk<SwapperProviderType> {
            every { protocol } returns "SushiSwap"
            every { name } returns "SushiSwap"
        }
        
        val uniswapData = mockk<SwapperProviderData> {
            every { provider } returns uniswapProvider
        }
        
        val sushiData = mockk<SwapperProviderData> {
            every { provider } returns sushiProvider
        }

        val mockRequest = mockk<SwapperQuoteRequest>()
        
        val uniswapQuote = mockk<SwapperQuote> {
            every { toValue } returns "1500000000000000000" // 1.5 ETH
            every { data } returns uniswapData
            every { request } returns mockRequest
            every { fromValue } returns "1000000000000000000"
            every { etaInSeconds } returns null
        }
        
        val sushiQuote = mockk<SwapperQuote> {
            every { toValue } returns "1600000000000000000" // 1.6 ETH
            every { data } returns sushiData
            every { request } returns mockRequest
            every { fromValue } returns "1000000000000000000"
            every { etaInSeconds } returns null
        }

        val result = getProviders(
            items = listOf(uniswapQuote, sushiQuote),
            priceValue = 2500.0, // $2500 per ETH
            currency = Currency.USD,
            asset = createTestAsset(decimals = 18, symbol = "ETH")
        )

        assertEquals(2, result.size)
        
        // Check Uniswap quote
        val uniswapResult = result.find { it.swapProvider.protocol == "UniswapV3" }
        assertNotNull("Should have Uniswap result", uniswapResult)
        assertTrue("Uniswap price should contain '1.5'", uniswapResult!!.price?.contains("1.5") == true)
        assertTrue("Uniswap fiat should contain '3,750'", uniswapResult.fiat?.contains("3,750") == true) // 1.5 * 2500
        
        // Check SushiSwap quote
        val sushiResult = result.find { it.swapProvider.protocol == "SushiSwap" }
        assertNotNull("Should have SushiSwap result", sushiResult)
        assertTrue("SushiSwap price should contain '1.6'", sushiResult!!.price?.contains("1.6") == true)
        assertTrue("SushiSwap fiat should contain '4,000'", sushiResult.fiat?.contains("4,000") == true) // 1.6 * 2500
    }

    @Test
    fun `getProviders should handle different asset decimals correctly`() {
        val mockProviderType = mockk<SwapperProviderType> {
            every { protocol } returns "UniswapV3"
            every { name } returns "Uniswap V3"
        }
        
        val mockProviderData = mockk<SwapperProviderData> {
            every { provider } returns mockProviderType
        }

        val mockQuoteRequest = mockk<SwapperQuoteRequest>()
        
        val mockQuote = mockk<SwapperQuote> {
            every { toValue } returns "1000000" // 1.0 USDC (6 decimals)
            every { data } returns mockProviderData
            every { request } returns mockQuoteRequest
            every { fromValue } returns "1000000000000000000"
            every { etaInSeconds } returns null
        }

        val result = getProviders(
            items = listOf(mockQuote),
            priceValue = 1.0, // $1 per USDC
            currency = Currency.USD,
            asset = createTestAsset(decimals = 6, symbol = "USDC")
        )

        assertEquals(1, result.size)
        val providerItem = result.first()
        
        // Should format 1.0 USDC correctly
        assertNotNull("Price should not be null", providerItem.price)
        assertTrue("Price should contain '1', got: '${providerItem.price}'", 
            providerItem.price?.contains("1") == true)
        
        // Fiat value should be $1.00
        assertNotNull("Fiat should not be null", providerItem.fiat)
        assertTrue("Fiat should contain '1', got: '${providerItem.fiat}'", 
            providerItem.fiat?.contains("1") == true)
    }

    @Test
    fun `getProviders should handle zero price value`() {
        val mockProviderType = mockk<SwapperProviderType> {
            every { protocol } returns "UniswapV3"
            every { name } returns "Uniswap V3"
        }
        
        val mockProviderData = mockk<SwapperProviderData> {
            every { provider } returns mockProviderType
        }

        val mockQuoteRequest = mockk<SwapperQuoteRequest>()
        
        val mockQuote = mockk<SwapperQuote> {
            every { toValue } returns "1000000000000000000" // 1.0 ETH
            every { data } returns mockProviderData
            every { request } returns mockQuoteRequest
            every { fromValue } returns "1000000000000000000"
            every { etaInSeconds } returns null
        }

        val result = getProviders(
            items = listOf(mockQuote),
            priceValue = 0.0, // No price data
            currency = Currency.USD,
            asset = createTestAsset(decimals = 18, symbol = "ETH")
        )

        assertEquals(1, result.size)
        val providerItem = result.first()
        
        // Price should still show crypto amount
        assertNotNull("Price should not be null", providerItem.price)
        assertTrue("Price should contain '1', got: '${providerItem.price}'", 
            providerItem.price?.contains("1") == true)
        
        // Fiat should be $0.00 or empty
        assertNotNull("Fiat should not be null", providerItem.fiat)
    }

    @Test
    fun `getProviders should handle different currencies`() {
        val mockProviderType = mockk<SwapperProviderType> {
            every { protocol } returns "UniswapV3"
            every { name } returns "Uniswap V3"
        }
        
        val mockProviderData = mockk<SwapperProviderData> {
            every { provider } returns mockProviderType
        }

        val mockQuoteRequest = mockk<SwapperQuoteRequest>()
        
        val mockQuote = mockk<SwapperQuote> {
            every { toValue } returns "2000000000000000000" // 2.0 ETH
            every { data } returns mockProviderData
            every { request } returns mockQuoteRequest
            every { fromValue } returns "1000000000000000000"
            every { etaInSeconds } returns null
        }

        // Test with EUR
        val resultEUR = getProviders(
            items = listOf(mockQuote),
            priceValue = 2800.0, // €2800 per ETH
            currency = Currency.EUR,
            asset = createTestAsset(decimals = 18, symbol = "ETH")
        )

        assertEquals(1, resultEUR.size)
        val providerItemEUR = resultEUR.first()
        
        // Should format as EUR currency (2.0 ETH * €2800 = €5600)
        assertNotNull("EUR fiat should not be null", providerItemEUR.fiat)
        assertTrue("EUR fiat should contain '5,600', got: '${providerItemEUR.fiat}'", 
            providerItemEUR.fiat?.contains("5,600") == true)
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