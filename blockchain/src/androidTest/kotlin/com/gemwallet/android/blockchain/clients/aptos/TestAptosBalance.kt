package com.gemwallet.android.blockchain.clients.aptos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAptosBalance {

    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testAptosBalance() {
        val balancesService = TestAptosBalancesService(nativeBalance = "100000000")
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = balancesService
        )

        val result = runBlocking {
            client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
        }
        assertNotNull(result)
        assertEquals("0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", balancesService.nativeRequest)
        assertEquals("100000000", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(1.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(1.0, result.totalAmount)
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testAptosBalanceFail() {
        val balanceService = TestAptosBalancesService()
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = balanceService
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
            assertNull(result)
        }
    }

    @Test
    fun testAptosBalanceBadValue() {
        val balanceService = TestAptosBalancesService(nativeBalance = "0abcde")
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = balanceService
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
            assertNull(result)
        }
    }

    @Test
    fun testAptosBalanceEmpty() {
        val balanceService = TestAptosBalancesService(nativeBalance = "0")
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = balanceService
        )

        val result = runBlocking {
            client.getNativeBalance(
                Chain.Aptos,
                "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f"
            )
        }
        assertNotNull(result)
        assertEquals("0", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(0.0, result.totalAmount)
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testAptos_tokens_balance() {
        val balanceService = TestAptosBalancesService(
            nativeBalance = "0",
        )
        val assets = listOf(
            Asset(
                AssetId(Chain.Aptos, "0xe4ccb6d39136469f376242c31b34d10515c8eaaa38092f804db8e08a8f53c5b2::assets_v1::EchoCoin002"),
                decimals = 6,
                name = "Gui Inu",
                symbol = "GUI",
                type = AssetType.TOKEN
            ),
            Asset(
                AssetId(Chain.Aptos, "0x159df6b7689437016108a019fd5bef736bac692b6d4a1f10c941f6fbb9a74ca6::oft::CakeOFT"),
                decimals = 8,
                name = "PancakeSwap Token",
                symbol = "CAKE",
                type = AssetType.TOKEN
            ),
        )
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = balanceService
        )

        val result = runBlocking {
            client.getTokenBalances(
                Chain.Aptos,
                "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f",
                assets,
            )
        }
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(assets[0].id.toIdentifier(), result[0].asset.id.toIdentifier())
        assertEquals("30000000", result[0].balance.available)
        assertEquals(30.0, result[0].balanceAmount.available)
        assertEquals(assets[1].id.toIdentifier(), result[1].asset.id.toIdentifier())
        assertEquals("50000000", result[1].balance.available)
        assertEquals(0.5, result[1].balanceAmount.available)
    }
}