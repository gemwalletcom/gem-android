package com.gemwallet.android.blockchain.clients.aptos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.aptos.services.AptosBalancesService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.blockchain.aptos.models.AptosResource
import com.wallet.core.blockchain.aptos.models.AptosResourceBalance
import com.wallet.core.blockchain.aptos.models.AptosResourceCoin
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = object : AptosBalancesService {
                override suspend fun balance(address: String): Result<AptosResource<AptosResourceBalance>> {
                    assertEquals("0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", address)
                    return Result.success(
                        AptosResource(
                            type = "",
                            data = AptosResourceBalance(
                                coin = AptosResourceCoin(
                                    value = "100000000"
                                )
                            )
                        )
                    )
                }
            }
        )

        val result = runBlocking {
            client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
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
        assertEquals(AssetId(Chain.Aptos).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testAptosBalanceFail() {
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = object : AptosBalancesService {
                override suspend fun balance(address: String): Result<AptosResource<AptosResourceBalance>> {
                    assertEquals("0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f", address)
                    return Result.failure(Exception())
                }
            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
            assertNull(result)
        }
    }

    @Test
    fun testAptosBalanceBadValue() {
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = object : AptosBalancesService {
                override suspend fun balance(address: String): Result<AptosResource<AptosResourceBalance>> {
                    return Result.success(
                        AptosResource(
                            type = "",
                            data = AptosResourceBalance(
                                coin = AptosResourceCoin(
                                    value = "0abcde"
                                )
                            )
                        )
                    )
                }
            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
            assertNull(result)
        }
    }

    @Test
    fun testAptosBalanceEmpty() {
        val client = AptosBalanceClient(
            chain = Chain.Aptos,
            balanceService = object : AptosBalancesService {
                override suspend fun balance(address: String): Result<AptosResource<AptosResourceBalance>> {
                    return Result.success(
                        AptosResource(
                            type = "",
                            data = AptosResourceBalance(
                                coin = AptosResourceCoin(
                                    value = "0"
                                )
                            )
                        )
                    )
                }

            }
        )

        runBlocking {
            val result = client.getNativeBalance(Chain.Aptos, "0x80c3cca35602e4568a7ac88d4d91110f8efa6c45c659439c2b4ed04033059c6f")
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
    }
}