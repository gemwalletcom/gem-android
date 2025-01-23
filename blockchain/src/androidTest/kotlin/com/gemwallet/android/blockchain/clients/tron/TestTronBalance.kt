package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.includeLibs
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronFrozen
import com.wallet.core.blockchain.tron.models.TronSmartContractResult
import com.wallet.core.blockchain.tron.models.TronSmartContractResultMessage
import com.wallet.core.blockchain.tron.models.TronUnfrozen
import com.wallet.core.blockchain.tron.models.TronVote
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

class TestTronBalance {
    companion object {
        init {
            includeLibs()
        }
    }

    val stakeService = FakeTronStakeService(0xF4240)

    @Test
    fun testTron_native_balance_success_and_not_empty() {
        val accountService = FakeTronAccountService(
            TronAccount(
                balance = 1_000_000,
            )
        )
        val callService = FakeTronCallService()
        val balanceClient = TronBalanceClient(Chain.Tron, accountService, callService, FakeTronStakeService())
        val result = runBlocking { balanceClient.getNativeBalance(Chain.Tron, "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU") }
        assertEquals("TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU", accountService.accountRequest?.address)
        assertTrue(accountService.accountRequest!!.visible)
        assertNotNull(result)
        assertEquals(AssetId(Chain.Tron), result!!.asset.id)
        assertEquals("1000000", result.balance.available)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.rewards)
        assertEquals(1.0, result.balanceAmount.available)
        assertEquals(1.0, result.totalAmount)
    }

    @Test
    fun testTron_native_balance_success_and_empty() {
        val accountService = FakeTronAccountService(
            TronAccount()
        )
        val callService = FakeTronCallService()
        val balanceClient = TronBalanceClient(Chain.Tron, accountService, callService, FakeTronStakeService())
        val result = runBlocking { balanceClient.getNativeBalance(Chain.Tron, "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU") }
        assertNull(result)
    }

    @Test
    fun testTron_native_balance_fail() {
        val accountService = FakeTronAccountService()
        val callService = FakeTronCallService()
        val balanceClient = TronBalanceClient(Chain.Tron, accountService, callService, stakeService)
        val result = runBlocking { balanceClient.getNativeBalance(Chain.Tron, "") }
        assertNull(result)
    }

    @Test
    fun testTron_token_balance_success() {
        val assets = listOf(
            Asset(
                id = AssetId(Chain.Tron, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"),
                decimals = 6,
                name = "Tether USD",
                symbol = "USDT",
                type = AssetType.TRC20,
            ),
            Asset(
                id = AssetId(Chain.Tron, "TAFjULxiVgT4qWk6UZwjqwZXTSaGaqnVp4"),
                decimals = 18,
                name = "BitTorrent",
                symbol = "BTT",
                type = AssetType.TRC20,
            ),
            Asset(
                id = AssetId(Chain.Tron, "TG6jUMfwpwR9QNFsSwCGtLaV2TR2gV8yru"),
                decimals = 6,
                name = "Wrapped TRX",
                symbol = "WTRX",
                type = AssetType.TRC20,
            ),
        )
        val accountService = FakeTronAccountService(TronAccount(balance = 1_000_000))
        val callService = FakeTronCallService(
            mapOf(
                "41a614f803b6fd780986a42c78ec9c7f77e6ded13c" to TronSmartContractResult(
                    result = TronSmartContractResultMessage(true),
                    constant_result = listOf("F4240"),
                    energy_used = 1,
                ),
                "41032017411f4663b317fe77c257d28d5cd1b26e3d" to TronSmartContractResult(
                    result = TronSmartContractResultMessage(true),
                    constant_result = listOf("DE0B6B3A7640000"),
                    energy_used = 1,
                ),
                "41433d1a20a144c3b064e9371bca739f960900f409" to TronSmartContractResult(
                    result = TronSmartContractResultMessage(true),
                    constant_result = listOf("3000000"),
                    energy_used = 1,
                ),
            )
        )
        val balanceClient = TronBalanceClient(Chain.Tron, accountService, callService, stakeService)
        val result = runBlocking { balanceClient.getTokenBalances(Chain.Tron, "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU", assets) }
        assertEquals("41a614f803b6fd780986a42c78ec9c7f77e6ded13c", callService.requests[0]["contract_address"])
        assertEquals("41032017411f4663b317fe77c257d28d5cd1b26e3d", callService.requests[1]["contract_address"])
        assertEquals("41433d1a20a144c3b064e9371bca739f960900f409", callService.requests[2]["contract_address"])
        assertEquals(false, callService.requests[0]["visible"])
        assertEquals(0L, callService.requests[0]["call_value"])
        assertEquals("4187b59ec7bb58250533fb8235f9a3f8eec9c1bfe8", callService.requests[0]["owner_address"])
        assertEquals(1_000_000L, callService.requests[0]["fee_limit"])
        assertEquals("balanceOf(address)", callService.requests[0]["function_selector"])
        assertEquals("00000000000000000000004187b59ec7bb58250533fb8235f9a3f8eec9c1bfe8", callService.requests[0]["parameter"])

        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("1000000", result[0].balance.available)
        assertEquals(1.0, result[0].balanceAmount.available)
        assertEquals("1000000000000000000", result[1].balance.available)
        assertEquals(1.0, result[1].balanceAmount.available)
        assertEquals("50331648", result[2].balance.available)
        assertEquals(50.331648, result[2].balanceAmount.available)
    }

    @Test
    fun testTron_stake_balance_success() {
        val accountService = FakeTronAccountService(
            TronAccount(
                balance = 1_000_000,
                frozenV2 = listOf(
                    TronFrozen(
                        amount = 0xF4240,
                    ),
                    TronFrozen(
                        amount = 0x1E8480,
                    ),
                ),
                unfrozenV2 = listOf(
                    TronUnfrozen(
                        unfreeze_amount = 0x2DC6C0
                    ),
                    TronUnfrozen(
                        unfreeze_amount = 0x3D0900
                    ),
                ),
                votes = listOf(
                    TronVote(
                        vote_address = "",
                        vote_count = 1,
                    ),
                    TronVote(
                        vote_address = "",
                        vote_count = 2,
                    ),
                    TronVote(
                        vote_address = "",
                        vote_count = 3,
                    ),
                )
            )
        )
        val callService = FakeTronCallService()
        val balanceClient = TronBalanceClient(Chain.Tron, accountService, callService, stakeService)
        val result = runBlocking { balanceClient.getNativeBalance(Chain.Tron, "TNLmo9j9AuGnnxibQUT13xoMGuUmNwxtkU") }
        assertNotNull(result)
        assertEquals(AssetId(Chain.Tron), result!!.asset.id)
        assertEquals("1000000", result.balance.available)
        assertEquals("6000000", result.balance.staked)
        assertEquals("7000000", result.balance.pending)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.frozen)
        assertEquals("1000000", result.balance.rewards)
        assertEquals(1.0, result.balanceAmount.available)
        assertEquals(6.0, result.balanceAmount.staked)
        assertEquals(7.0, result.balanceAmount.pending)
        assertEquals(1.0, result.balanceAmount.rewards)
        assertEquals(15.0, result.totalAmount)
    }
}