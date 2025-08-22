package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosStakeService
import com.wallet.core.blockchain.cosmos.CosmosBalance
import com.wallet.core.blockchain.cosmos.CosmosDelegation
import com.wallet.core.blockchain.cosmos.CosmosDelegationData
import com.wallet.core.blockchain.cosmos.CosmosDelegations
import com.wallet.core.blockchain.cosmos.CosmosReward
import com.wallet.core.blockchain.cosmos.CosmosRewards
import com.wallet.core.blockchain.cosmos.CosmosUnboudingDelegationEntry
import com.wallet.core.blockchain.cosmos.CosmosUnboundingDelegation
import com.wallet.core.blockchain.cosmos.CosmosUnboundingDelegations
import com.wallet.core.blockchain.cosmos.CosmosValidator
import com.wallet.core.blockchain.cosmos.CosmosValidatorCommission
import com.wallet.core.blockchain.cosmos.CosmosValidatorCommissionRates
import com.wallet.core.blockchain.cosmos.CosmosValidatorMoniker
import com.wallet.core.blockchain.cosmos.CosmosValidators
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestCosmosStake {

    private class TestCosmosStakeService(
        val validators: List<CosmosValidator> = listOf(
            CosmosValidator(
                operator_address = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                jailed = false,
                status = "BOND_STATUS_BONDED",
                description = CosmosValidatorMoniker("stakebeast"),
                commission = CosmosValidatorCommission(CosmosValidatorCommissionRates(rate = "0.050000000000000000"))
            ),
            CosmosValidator(
                operator_address = "osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2",
                jailed = false,
                status = "BOND_STATUS_UBONDED",
                description = CosmosValidatorMoniker("Redline Validation"),
                commission = CosmosValidatorCommission(CosmosValidatorCommissionRates(rate = "1.000000000000000000"))
            ),
            CosmosValidator(
                operator_address = "osmovaloper1wgmdcxzp49vjgrqusgcagq6qefk4mtjv5c0k7q",
                jailed = true,
                status = "BOND_STATUS_UNBONDED",
                description = CosmosValidatorMoniker("Dystopia Labs Validator"),
                commission = CosmosValidatorCommission(CosmosValidatorCommissionRates(rate = "0.040000000000000000"))
            ),
            CosmosValidator(
                operator_address = "osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm",
                jailed = false,
                status = "BOND_STATUS_BONDED",
                description = CosmosValidatorMoniker("Cypher Core"),
                commission = CosmosValidatorCommission(CosmosValidatorCommissionRates(rate = "0.100000000000000000"))
            ),
        ),
        val delegations: List<CosmosDelegation> = emptyList(),
        val unboundings: List<CosmosUnboundingDelegation> = emptyList(),
        val rewards: List<CosmosReward> = emptyList(),
    ) : CosmosStakeService {

        var delegationsAddressRequest: String = ""
        var unboundingAddressRequest: String = ""
        var rewardsAddressRequest: String = ""

        override suspend fun validators(): Result<CosmosValidators> {
            return Result.success(CosmosValidators(validators))
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
    fun testGetValidators() {
        val client = CosmosStakeClient(
            chain = Chain.Osmosis,
            stakeService = TestCosmosStakeService()
        )
        val result = runBlocking { client.getValidators(Chain.Osmosis, 5.68) }
        assertFalse(result.isEmpty())
        assertEquals(4, result.size)
        for (validator in result) {
            assertEquals(Chain.Osmosis, validator.chain)
        }
        assertTrue(result[0].isActive)
        assertFalse(result[1].isActive)
        assertFalse(result[2].isActive)
        assertTrue(result[3].isActive)
        assertEquals("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm", result[0].id)
        assertEquals("osmovaloper1z0sh4s80u99l6y9d3vfy582p8jejeeu6tcucs2", result[1].id)
        assertEquals("Redline Validation", result[1].name)
        assertEquals("Cypher Core", result[3].name)
        assertEquals(0.05, result[0].commision)
        assertEquals(0.04, result[2].commision)
        assertEquals(5.396, result[0].apr)
        assertEquals(5.112, result[3].apr)
    }

    @Test
    fun testGetValidatorsEmpty() {
        val client = CosmosStakeClient(
            chain = Chain.Osmosis,
            stakeService = TestCosmosStakeService(emptyList())
        )
        val result = runBlocking { client.getValidators(Chain.Osmosis, 5.68) }
        assertTrue(result.isEmpty())
    }

    @Test
    fun testGetDelegations() {
        val client = CosmosStakeClient(
            Chain.Osmosis,
            TestCosmosStakeService(
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
                                creation_height = "25053096",
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
        )
        val result = runBlocking { client.getStakeDelegations(Chain.Osmosis, "osmo1ac4ns740p4v78n4wr7j3t3s79jjjre6udx7n2v", 5.68) }
        assertEquals(4, result.size)
        assertEquals(AssetId(Chain.Osmosis), result[0].assetId)
        assertEquals(DelegationState.Active, result[0].state)
        assertEquals(DelegationState.Pending, result[3].state)
        assertEquals("1000000", result[0].balance)
        assertEquals("2000000", result[3].balance)
        assertEquals("808", result[0].rewards)
        assertEquals("808", result[3].rewards)
        assertNull(result[0].completionDate)
        assertEquals(1735138660866, result[3].completionDate)
        assertEquals("", result[0].delegationId)
        assertEquals("osmovaloper1pxphtfhqnx9ny27d53z4052e3r76e7qq495ehm", result[0].validatorId)
    }
}