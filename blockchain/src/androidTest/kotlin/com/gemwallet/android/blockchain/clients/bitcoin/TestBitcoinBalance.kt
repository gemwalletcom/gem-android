package com.gemwallet.android.blockchain.clients.bitcoin

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinBalancesService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.blockchain.bitcoin.models.BitcoinAccount
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestBitcoinBalance {

    companion object {
        init {
            includeLibs()
        }
    }

    @Test
    fun testBitcoinBalance() {
        val client = BitcoinBalanceClient(
            chain = Chain.Bitcoin,
            balanceService = object : BitcoinBalancesService {
                override suspend fun balance(address: String): Result<BitcoinAccount> {
                    assertEquals("bc1qqypy0q4uwk8h845j8qc5r76zyk2fwdtvqy4s4x", address)
                    return Result.success(BitcoinAccount(balance = "100000000"))
                }
            }
        )

        val result = runBlocking {
            client.getNativeBalance(Chain.Bitcoin, "bc1qqypy0q4uwk8h845j8qc5r76zyk2fwdtvqy4s4x")
        }
        assertNotNull(result)
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
        assertEquals(AssetId(Chain.Bitcoin).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testBitcoinBalanceFail() {
        val client = BitcoinBalanceClient(
            chain = Chain.Bitcoin,
            balanceService = object : BitcoinBalancesService {
                override suspend fun balance(address: String): Result<BitcoinAccount> {
                    return Result.failure(Exception())
                }
            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Bitcoin, "bc1qqypy0q4uwk8h845j8qc5r76zyk2fwdtvqy4s4x")
            assertNull(result)
        }
    }

    @Test
    fun testBitcoinBalanceBadValue() {
        val client = BitcoinBalanceClient(
            chain = Chain.Bitcoin,
            balanceService = object : BitcoinBalancesService {
                override suspend fun balance(address: String): Result<BitcoinAccount> {
                    return Result.success(BitcoinAccount("0abcdesdf"))
                }
            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Bitcoin, "bc1qqypy0q4uwk8h845j8qc5r76zyk2fwdtvqy4s4x")
            assertNull(result)
        }
    }

    @Test
    fun testBitcoinBalanceEmpty() {
        val client = BitcoinBalanceClient(
            chain = Chain.Bitcoin,
            balanceService = object : BitcoinBalancesService {
                override suspend fun balance(address: String): Result<BitcoinAccount> {
                    return Result.success(BitcoinAccount("0"))
                }

            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Bitcoin, "bc1qqypy0q4uwk8h845j8qc5r76zyk2fwdtvqy4s4x")
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
            assertEquals(AssetId(Chain.Bitcoin).toIdentifier(), result.asset.id.toIdentifier())
        }
    }
}