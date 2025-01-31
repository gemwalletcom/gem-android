package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaBalancesService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaStakeService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.gemwallet.android.model.getTotalAmount
import com.wallet.core.blockchain.solana.models.SolanaBalance
import com.wallet.core.blockchain.solana.models.SolanaBalanceValue
import com.wallet.core.blockchain.solana.models.SolanaEpoch
import com.wallet.core.blockchain.solana.models.SolanaStakeAccount
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountData
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountDataParsed
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountDataParsedInfo
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountDataParsedInfoMeta
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountDataParsedInfoStake
import com.wallet.core.blockchain.solana.models.SolanaStakeAccountDataParsedInfoStakeDelegation
import com.wallet.core.blockchain.solana.models.SolanaTokenAccountResult
import com.wallet.core.blockchain.solana.models.SolanaValidator
import com.wallet.core.blockchain.solana.models.SolanaValidators
import com.wallet.core.blockchain.solana.models.SolanaValue
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestSolanaBalances {

    companion object {
        init {
            includeLibs()
        }
    }

    private class TestBalancesService : SolanaBalancesService {
        var nativeRequest: JSONRpcRequest<List<String>>? = null
        var tokenRequest: JSONRpcRequest<List<String>>? = null

        override suspend fun getBalance(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaBalance>> {
            nativeRequest = request
            return Result.success(
                JSONRpcResponse(
                    SolanaBalance(1_000_000)
                )
            )
        }

        override suspend fun getTokenBalance(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaValue<SolanaBalanceValue>>> {
            tokenRequest = request
            return Result.success(
                JSONRpcResponse(
                    SolanaValue(
                        SolanaBalanceValue(
                            when (request.params[0]) {
                                "HZ1JovNiVvGrGNiiYvEozEVgZ58xaU3RKwX8eACQBCt3" -> "5000000"
                                else -> "300000"
                            }
                        )
                    )
                )
            )
        }
    }

    private class TestStakeService(
        private val delegationsResponse: JSONRpcResponse<List<SolanaTokenAccountResult<SolanaStakeAccount>>> = JSONRpcResponse(emptyList())
    ) : SolanaStakeService {
        var validatorsRequest: JSONRpcRequest<List<Any>>? = null
        var delegationsRequest: JSONRpcRequest<List<Any>>? = null

        override suspend fun validators(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValidators>> {
            validatorsRequest = request
            return Result.success(
                JSONRpcResponse(
                    SolanaValidators(
                        listOf(
                            SolanaValidator("validatorPubKey", 10, true)
                        )
                    )
                )
            )
        }

        override suspend fun delegations(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<List<SolanaTokenAccountResult<SolanaStakeAccount>>>> {
            delegationsRequest = request

            return Result.success(delegationsResponse)
        }

        override suspend fun epoch(request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaEpoch>> {
            return Result.success(
                JSONRpcResponse(
                    SolanaEpoch(
                        epoch = 10,
                        slotsInEpoch = 1,
                        slotIndex = 1,
                    )
                )
            )
        }

    }

    @Test
    fun testSolana_balance_native() {
        val accountsService = TestSolanaAccountsService()
        val balancesService = TestBalancesService()
        val stakeService = TestStakeService()

        val balanceClient = SolanaBalanceClient(
            chain = Chain.Solana,
            accountsService = accountsService,
            balancesService = balancesService,
            stakeService = stakeService,
        )
        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")
        }
        assertEquals("AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh", balancesService.nativeRequest!!.params[0])
        assertNotNull(result)
        assertEquals(AssetId(Chain.Solana), result!!.asset.id)
        assertEquals("1000000", result.balance.available)
        assertEquals("0", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.rewards)
    }

    @Test
    fun testSolana_balance_token() {
        val accountsService = TestSolanaAccountsService()
        val balancesService = TestBalancesService()
        val stakeService = TestStakeService()

        val balanceClient = SolanaBalanceClient(
            chain = Chain.Solana,
            accountsService = accountsService,
            balancesService = balancesService,
            stakeService = stakeService,
        )
        val result = runBlocking {
            balanceClient.getTokenBalances(
                Chain.Solana,
                "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh",
                listOf(
                    Asset(
                        id = AssetId(Chain.Solana, "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"),
                        name = "Jupiter",
                        symbol = "JUP",
                        decimals = 6,
                        type = AssetType.TOKEN,
                    ),
                    Asset(
                        id = AssetId(Chain.Solana, "HZ1JovNiVvGrGNiiYvEozEVgZ58xaU3RKwX8eACQBCt3"),
                        name = "Pyth Network ",
                        symbol = "PYTH",
                        decimals = 6,
                        type = AssetType.TOKEN,
                    ),
                )
            )
        }
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(AssetId(Chain.Solana, "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"), result[0].asset.id)
        assertEquals(AssetId(Chain.Solana, "HZ1JovNiVvGrGNiiYvEozEVgZ58xaU3RKwX8eACQBCt3"), result[1].asset.id)
        assertEquals("300000", result[0].balance.available)
        assertEquals("5000000", result[1].balance.available)
    }

    @Test
    fun testSolana_balance_native_width_single_stake() {
        val accountsService = TestSolanaAccountsService()
        val balancesService = TestBalancesService()
        val stakeService = TestStakeService(
            delegationsResponse = JSONRpcResponse(
                listOf(
                    SolanaTokenAccountResult(
                        SolanaStakeAccount(
                            1000000,
                            10,
                            SolanaStakeAccountData(
                                SolanaStakeAccountDataParsed(
                                    SolanaStakeAccountDataParsedInfo(
                                        SolanaStakeAccountDataParsedInfoStake(
                                            SolanaStakeAccountDataParsedInfoStakeDelegation("", "", "", ""),
                                        ),
                                        SolanaStakeAccountDataParsedInfoMeta("")
                                    )
                                )
                            )
                        ),
                        pubkey = "",
                    )
                )

            )
        )

        val balanceClient = SolanaBalanceClient(
            chain = Chain.Solana,
            accountsService = accountsService,
            balancesService = balancesService,
            stakeService = stakeService,
        )
        val result = runBlocking {
            balanceClient.getDelegationBalances(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")
        }
        assertNotNull(result)
        assertEquals(AssetId(Chain.Solana), result!!.asset.id)
        assertEquals("0", result.balance.available)
        assertEquals("1000000", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.rewards)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.001, result.balanceAmount.staked)
        assertEquals(0.001, result.balanceAmount.getTotalAmount())
    }

    @Test
    fun testSolana_balance_native_width_multi_stake() {
        val accountsService = TestSolanaAccountsService()
        val balancesService = TestBalancesService()
        val stakeService = TestStakeService(
            delegationsResponse = JSONRpcResponse(
                listOf(
                    SolanaTokenAccountResult(
                        SolanaStakeAccount(
                            1000000,
                            10,
                            SolanaStakeAccountData(
                                SolanaStakeAccountDataParsed(
                                    SolanaStakeAccountDataParsedInfo(
                                        SolanaStakeAccountDataParsedInfoStake(
                                            SolanaStakeAccountDataParsedInfoStakeDelegation("", "", "", ""),
                                        ),
                                        SolanaStakeAccountDataParsedInfoMeta("")
                                    )
                                )
                            )
                        ),
                        pubkey = "",
                    ),
                    SolanaTokenAccountResult(
                        SolanaStakeAccount(
                            2000000,
                            20,
                            SolanaStakeAccountData(
                                SolanaStakeAccountDataParsed(
                                    SolanaStakeAccountDataParsedInfo(
                                        SolanaStakeAccountDataParsedInfoStake(
                                            SolanaStakeAccountDataParsedInfoStakeDelegation("", "", "", ""),
                                        ),
                                        SolanaStakeAccountDataParsedInfoMeta("")
                                    )
                                )
                            )
                        ),
                        pubkey = "",
                    )
                )

            )
        )

        val balanceClient = SolanaBalanceClient(
            chain = Chain.Solana,
            accountsService = accountsService,
            balancesService = balancesService,
            stakeService = stakeService,
        )
        val result = runBlocking {
            balanceClient.getDelegationBalances(Chain.Solana, "AGkXQZ9qm99xukisDUHvspWHESrcjs8Y4AmQQgef3BRh")
        }
        assertNotNull(result)
        assertEquals(AssetId(Chain.Solana), result!!.asset.id)
        assertEquals("0", result.balance.available)
        assertEquals("3000000", result.balance.staked)
        assertEquals("0", result.balance.pending)
        assertEquals("0", result.balance.reserved)
        assertEquals("0", result.balance.locked)
        assertEquals("0", result.balance.frozen)
        assertEquals("0", result.balance.rewards)
        assertEquals(0.0, result.balanceAmount.available)
        assertEquals(0.003, result.balanceAmount.staked)
        assertEquals(0.003, result.balanceAmount.getTotalAmount())
    }
}