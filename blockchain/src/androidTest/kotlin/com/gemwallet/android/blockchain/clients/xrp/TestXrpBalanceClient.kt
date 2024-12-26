package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.blockchain.xrp.models.XRPAccount
import com.wallet.core.blockchain.xrp.models.XRPAccountResult
import com.wallet.core.blockchain.xrp.models.XRPResult
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestXrpBalanceClient {
    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testXrp_native_balance() {
        val accountsService = TestXrpAccountsService("2104374")
        val balanceClient = XrpBalanceClient(
            chain = Chain.Xrp,
            accountsService = accountsService
        )
        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Xrp, "rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN")
        }
        assertEquals("rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN", accountsService.requestAccount)
        assertNotNull(result)
        assertEquals("1104374", result.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("1000000", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(1.104374, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(1.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(1.104374, result.totalAmount)
        assertEquals(AssetId(Chain.Xrp).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testXrp_native_fail_balance() {
        val accountsService = TestXrpAccountsService("")
        val balanceClient = XrpBalanceClient(
            chain = Chain.Xrp,
            accountsService = accountsService
        )
        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Xrp, "rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN")
        }
        assertEquals("rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN", accountsService.requestAccount)
        assertNotNull(result)
        assertEquals("0", result.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("1000000", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(1.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(0.0, result.totalAmount)
        assertEquals(AssetId(Chain.Xrp).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testXrp_native_zero_balance() {
        val accountsService = TestXrpAccountsService("1000000")
        val balanceClient = XrpBalanceClient(
            chain = Chain.Xrp,
            accountsService = accountsService
        )
        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Xrp, "rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN")
        }
        assertEquals("rwCq6DCHa6HPFyfvabvzcX1y5wABw5KdiN", accountsService.requestAccount)
        assertNotNull(result)
        assertEquals("0", result.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("1000000", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(1.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(0.0, result.totalAmount)
        assertEquals(AssetId(Chain.Xrp).toIdentifier(), result.asset.id.toIdentifier())
    }
}