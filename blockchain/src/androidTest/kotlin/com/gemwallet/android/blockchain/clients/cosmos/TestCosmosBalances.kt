package com.gemwallet.android.blockchain.clients.cosmos

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosBalancesService
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosStakeService
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.blockchain.cosmos.models.CosmosBalance
import com.wallet.core.blockchain.cosmos.models.CosmosBalances
import com.wallet.core.blockchain.cosmos.models.CosmosDelegation
import com.wallet.core.blockchain.cosmos.models.CosmosDelegationData
import com.wallet.core.blockchain.cosmos.models.CosmosDelegations
import com.wallet.core.blockchain.cosmos.models.CosmosReward
import com.wallet.core.blockchain.cosmos.models.CosmosRewards
import com.wallet.core.blockchain.cosmos.models.CosmosUnboudingDelegationEntry
import com.wallet.core.blockchain.cosmos.models.CosmosUnboundingDelegation
import com.wallet.core.blockchain.cosmos.models.CosmosUnboundingDelegations
import com.wallet.core.blockchain.cosmos.models.CosmosValidators
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestCosmosBalances {

    private class TestCosmosStakeService(
        val delegations: List<CosmosDelegation> = emptyList(),
        val unboundings: List<CosmosUnboundingDelegation> = emptyList(),
        val rewards: List<CosmosReward> = emptyList(),
    ) : CosmosStakeService {

        var delegationsAddressRequest: String = ""
        var unboundingAddressRequest: String = ""
        var rewardsAddressRequest: String = ""

        override suspend fun validators(): Result<CosmosValidators> {
            return Result.success(CosmosValidators(emptyList()))
        }

        override suspend fun delegations(address: String): Result<CosmosDelegations> {
            delegationsAddressRequest = address
            return Result.success(CosmosDelegations(delegations))
        }

        override suspend fun undelegations(address: String): Result<CosmosUnboundingDelegations> {
            unboundingAddressRequest = address
            return Result.success(CosmosUnboundingDelegations(unboundings))
        }

        override suspend fun rewards(address: String): Result<CosmosRewards> {
            rewardsAddressRequest = address
            return Result.success(CosmosRewards(rewards))
        }

    }

    @Test
    fun testCosmosBalance() {
        var ownerRequest: String = ""
        val balanceService = object : CosmosBalancesService {
            override suspend fun getBalance(owner: String): Result<CosmosBalances> {
                ownerRequest = owner
                return Result.success(
                    CosmosBalances(
                        listOf(
                            CosmosBalance(denom = "uosmo", amount = "5272821")
                        )
                    )
                )
            }
        }

        val result = runBlocking {
            CosmosBalanceClient(
                Chain.Osmosis,
                balancesService = balanceService,
                stakeService = TestCosmosStakeService(),
            ).getNativeBalance(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v")
        }
        assertEquals("osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", ownerRequest)
        assertNotNull(result)
        assertEquals("5272821", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(5.272821, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(5.272821, result.totalAmount)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testCosmosBalanceStakeOnly() {
        var ownerRequest: String = ""
        val balanceService = object : CosmosBalancesService {
            override suspend fun getBalance(owner: String): Result<CosmosBalances> {
                ownerRequest = owner
                return Result.success(CosmosBalances(listOf()))
            }
        }

        val stakeService = TestCosmosStakeService(
            delegations = listOf(
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm"),
                    CosmosBalance("uosmo", "1000000")
                ),
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2"),
                    CosmosBalance("uosmo", "1000000")
                ),
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1wgmdcxzp49vjgrqusgcagq6qefk4mtjv5c0k7q"),
                    CosmosBalance("uosmo", "721000000")
                )
            )
        )

        val result = runBlocking {
            CosmosBalanceClient(
                Chain.Osmosis,
                balancesService = balanceService,
                stakeService = stakeService,
            ).getNativeBalance(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v")
        }
        assertEquals("osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", ownerRequest)
        assertNotNull(result)
        assertEquals("0", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("723000000", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(723.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(723.0, result.totalAmount)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testCosmosBalanceUnboundingOnly() {
        var ownerRequest: String = ""
        val balanceService = object : CosmosBalancesService {
            override suspend fun getBalance(owner: String): Result<CosmosBalances> {
                ownerRequest = owner
                return Result.success(CosmosBalances(listOf()))
            }
        }
        val stakeService = TestCosmosStakeService(
            unboundings = listOf(
                CosmosUnboundingDelegation(
                    "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                    entries = listOf(
                        CosmosUnboudingDelegationEntry(
                            creation_height	= "25053096",
                            completion_time = "2024-12-18T02:11:27.650773866Z",
                            balance = "2000000",
                        )
                    )
                ),
            )
        )

        val result = runBlocking {
            CosmosBalanceClient(
                Chain.Osmosis,
                balancesService = balanceService,
                stakeService = stakeService,
            ).getNativeBalance(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v")
        }
        assertEquals("osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", ownerRequest)
        assertNotNull(result)
        assertEquals("0", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("2000000", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.0, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(2.0, result.balanceAmount.pending)
        assertEquals(2.0, result.totalAmount)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testCosmosBalanceRewardsOnly() {
        var ownerRequest: String = ""
        val balanceService = object : CosmosBalancesService {
            override suspend fun getBalance(owner: String): Result<CosmosBalances> {
                ownerRequest = owner
                return Result.success(CosmosBalances(listOf()))
            }
        }
        val stakeService = TestCosmosStakeService(
            rewards = listOf(
                CosmosReward("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm", listOf(CosmosBalance(denom = "uosmo", amount = "808.015913259210000000"))),
                CosmosReward("osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2", listOf(CosmosBalance(denom = "uosmo", amount = "808.009961413523000000"))),
                CosmosReward("osmovaloper1wgmdcxzp49vjgrqusgcagq6qefk4mtjv5c0k7q", listOf(CosmosBalance(denom = "uosmo", amount = "582579.692713670953000000"))),
            )
        )

        val result = runBlocking {
            CosmosBalanceClient(
                Chain.Osmosis,
                balancesService = balanceService,
                stakeService = stakeService,
            ).getNativeBalance(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v")
        }
        assertEquals("osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", ownerRequest)
        assertNotNull(result)
        assertEquals("0", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.staked)
        assertEquals("584195", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.pending)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(0.0, result.balanceAmount.staked)
        assertEquals(0.584195, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(0.0, result.balanceAmount.pending)
        assertEquals(0.584195, result.totalAmount)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.asset.id.toIdentifier())
    }

    @Test
    fun testCosmosBalanceFull() {
        var ownerRequest: String = ""
        val balanceService = object : CosmosBalancesService {
            override suspend fun getBalance(owner: String): Result<CosmosBalances> {
                ownerRequest = owner
                return Result.success(
                    CosmosBalances(
                        listOf(
                            CosmosBalance(denom = "uosmo", amount = "5272821")
                        )
                    )
                )
            }
        }

        val stakeService = TestCosmosStakeService(
            delegations = listOf(
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm"),
                    CosmosBalance("uosmo", "1000000")
                ),
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2"),
                    CosmosBalance("uosmo", "1000000")
                ),
                CosmosDelegation(
                    CosmosDelegationData("osmovaloper1wgmdcxzp49vjgrqusgcagq6qefk4mtjv5c0k7q"),
                    CosmosBalance("uosmo", "721000000")
                )
            ),
            unboundings = listOf(
                CosmosUnboundingDelegation(
                    "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                    entries = listOf(
                        CosmosUnboudingDelegationEntry(
                            creation_height	= "25053096",
                            completion_time = "2024-12-18T02:11:27.650773866Z",
                            balance = "2000000",
                        )
                    )
                ),
            ),
            rewards = listOf(
                CosmosReward("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm", listOf(CosmosBalance(denom = "uosmo", amount = "808.015913259210000000"))),
                CosmosReward("osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2", listOf(CosmosBalance(denom = "uosmo", amount = "808.009961413523000000"))),
                CosmosReward("osmovaloper1wgmdcxzp49vjgrqusgcagq6qefk4mtjv5c0k7q", listOf(CosmosBalance(denom = "uosmo", amount = "582579.692713670953000000"))),
            )
        )

        val result = runBlocking {
            CosmosBalanceClient(
                Chain.Osmosis,
                balancesService = balanceService,
                stakeService = stakeService,
            ).getNativeBalance(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v")
        }
        assertEquals("osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", ownerRequest)
        assertNotNull(result)
        assertEquals("5272821", result!!.balance.available)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.locked)
        assertEquals("723000000", result.balance.staked)
        assertEquals("584195", result.balance.rewards)
        assertEquals("0", result.balance.reserved)
        assertEquals("2000000", result.balance.pending)
        assertEquals(5.272821, result.balanceAmount.available)
        assertEquals(0.0, result.balanceAmount.frozen)
        assertEquals(0.0, result.balanceAmount.locked)
        assertEquals(723.0, result.balanceAmount.staked)
        assertEquals(0.584195, result.balanceAmount.rewards)
        assertEquals(0.0, result.balanceAmount.reserved)
        assertEquals(2.0, result.balanceAmount.pending)
        assertEquals(730.857016, result.totalAmount)
        assertEquals(AssetId(Chain.Osmosis).toIdentifier(), result.asset.id.toIdentifier())
    }
}