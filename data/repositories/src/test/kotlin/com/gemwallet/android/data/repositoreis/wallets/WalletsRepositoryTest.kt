package com.gemwallet.android.data.repositoreis.wallets

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GenerateWalletIdTest {

    @Test
    fun `generateWalletId for Multicoin wallet type`() {
        val result = WalletsRepository.generateWalletId(WalletType.Multicoin, Chain.Ethereum, "0x1234567890abcdef")
        assertEquals("multicoin_0x1234567890abcdef", result)
    }

    @Test
    fun `generateWalletId for Multicoin ignores chain parameter`() {
        val address = "0xabcdef1234567890"
        val resultEthereum = WalletsRepository.generateWalletId(WalletType.Multicoin, Chain.Ethereum, address)
        val resultBitcoin = WalletsRepository.generateWalletId(WalletType.Multicoin, Chain.Bitcoin, address)
        assertEquals(resultEthereum, resultBitcoin)
        assertEquals("multicoin_$address", resultEthereum)
    }

    @Test
    fun `generateWalletId for Single wallet type with Ethereum`() {
        val result = WalletsRepository.generateWalletId(WalletType.Single, Chain.Ethereum, "0x1234567890abcdef")
        assertEquals("single_ethereum_0x1234567890abcdef", result)
    }

    @Test
    fun `generateWalletId for Single wallet type with Bitcoin`() {
        val result = WalletsRepository.generateWalletId(WalletType.Single, Chain.Bitcoin, "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh")
        assertEquals("single_bitcoin_bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", result)
    }

    @Test
    fun `generateWalletId for Single wallet type with Solana`() {
        val result = WalletsRepository.generateWalletId(WalletType.Single, Chain.Solana, "DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK")
        assertEquals("single_solana_DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", result)
    }

    @Test
    fun `generateWalletId for PrivateKey wallet type with Ethereum`() {
        val result = WalletsRepository.generateWalletId(WalletType.PrivateKey, Chain.Ethereum, "0xabcdef1234567890")
        assertEquals("privateKey_ethereum_0xabcdef1234567890", result)
    }

    @Test
    fun `generateWalletId for PrivateKey wallet type with Polygon`() {
        val result = WalletsRepository.generateWalletId(WalletType.PrivateKey, Chain.Polygon, "0x9876543210fedcba")
        assertEquals("privateKey_polygon_0x9876543210fedcba", result)
    }

    @Test
    fun `generateWalletId for View wallet type with Ethereum`() {
        val result = WalletsRepository.generateWalletId(WalletType.View, Chain.Ethereum, "0xfedcba0987654321")
        assertEquals("view_ethereum_0xfedcba0987654321", result)
    }

    @Test
    fun `generateWalletId for View wallet type with Tron`() {
        val result = WalletsRepository.generateWalletId(WalletType.View, Chain.Tron, "TRX123456789")
        assertEquals("view_tron_TRX123456789", result)
    }

    @Test
    fun `generateWalletId handles different chains correctly`() {
        val address = "test_address"
        val chains = listOf(
            Chain.Ethereum to "ethereum",
            Chain.Bitcoin to "bitcoin",
            Chain.Solana to "solana",
            Chain.Polygon to "polygon",
            Chain.Arbitrum to "arbitrum",
            Chain.Optimism to "optimism",
            Chain.Base to "base",
            Chain.Tron to "tron"
        )

        chains.forEach { (chain, expectedChainString) ->
            val result = WalletsRepository.generateWalletId(WalletType.Single, chain, address)
            assertEquals("single_${expectedChainString}_$address", result)
        }
    }

    @Test
    fun `generateWalletId throws exception for empty address`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            WalletsRepository.generateWalletId(WalletType.Multicoin, Chain.Ethereum, "")
        }
        assertEquals("Account address cannot be empty", exception.message)
    }

    @Test
    fun `generateWalletId for all WalletTypes maintains consistent format`() {
        val address = "0x1234567890abcdef"
        val chain = Chain.Ethereum

        val multicoin = WalletsRepository.generateWalletId(WalletType.Multicoin, chain, address)
        val single = WalletsRepository.generateWalletId(WalletType.Single, chain, address)
        val privateKey = WalletsRepository.generateWalletId(WalletType.PrivateKey, chain, address)
        val view = WalletsRepository.generateWalletId(WalletType.View, chain, address)

        assertEquals("multicoin_$address", multicoin)
        assertEquals("single_ethereum_$address", single)
        assertEquals("privateKey_ethereum_$address", privateKey)
        assertEquals("view_ethereum_$address", view)
    }
}