package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaBalancesService
import com.gemwallet.android.blockchain.clients.solana.services.SolanaStakeService
import com.gemwallet.android.blockchain.includeLibs
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
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
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaTokenAccountResult
import com.wallet.core.blockchain.solana.models.SolanaValidator
import com.wallet.core.blockchain.solana.models.SolanaValidators
import com.wallet.core.blockchain.solana.models.SolanaValue
import com.wallet.core.primitives.AssetId
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
                            "5000000"
                        )
                    )
                )
            )
        }
    }

    private class TestAccountsService : SolanaAccountsService {
        var tokenAccountRequest: JSONRpcRequest<List<Any>>? = null

        override suspend fun getTokenAccountByOwner(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>> {
            tokenAccountRequest = request
            return Result.success(
                JSONRpcResponse(
                    SolanaValue(
                        listOf(
                            SolanaTokenAccount("pubkey"),
                        )
                    )
                )
            )
        }
    }

    private class TestStakeService : SolanaStakeService {
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

            return Result.success(
                JSONRpcResponse(
                    emptyList()
                        /*SolanaTokenAccountResult(
                            SolanaStakeAccount(
                                100,
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
                        )*/
                )
            )
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
        val accountsService = TestAccountsService()
        val balancesService = TestBalancesService()
        val stakeService = TestStakeService()

        val balanceClient = SolanaBalanceClient(
            chain = Chain.Solana,
            accountsService = accountsService,
            balancesService = balancesService,
            stakeService = stakeService,
        )
        val result = runBlocking {
            balanceClient.getNativeBalance(Chain.Solana, "")
        }
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
}