package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.models.SolanaArrayData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedSplTokenInfo
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.SolanaAccount
import com.wallet.core.blockchain.solana.SolanaAccountParsed
import com.wallet.core.blockchain.solana.SolanaAccountParsedInfo
import com.wallet.core.blockchain.solana.SolanaTokenAccount
import com.wallet.core.blockchain.solana.SolanaTokenAmount
import com.wallet.core.blockchain.solana.SolanaTokenInfo
import com.wallet.core.blockchain.solana.SolanaValue

internal class TestSolanaAccountsService(
    private val tokenAddress: String? = null,
    private val owner: String = "",
) : SolanaAccountsService {
    var tokenAccountRequest: JSONRpcRequest<List<Any>>? = null

    override suspend fun getTokenAccountByOwner(request: JSONRpcRequest<List<Any>>): JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>> {
        tokenAccountRequest = request
        return JSONRpcResponse(
            SolanaValue(
                listOf(
                    SolanaTokenAccount(
                        pubkey = tokenAddress ?: "",
                        account = SolanaAccount(
                            lamports = 100000,
                            space = 10,
                            owner = owner,
                            SolanaAccountParsed(
                                SolanaAccountParsedInfo(
                                    SolanaTokenInfo(
                                        SolanaTokenAmount(tokenAddress ?: (request.params[1] as Map<*, *>)["mint"].toString())
                                    )
                                )
                            )
                        )
                    ),
                )
            )
        )
    }

    override suspend fun batchAccount(request: List<JSONRpcRequest<List<Any>>>): List<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>> {
        return request.mapIndexed { index, request ->
            JSONRpcResponse(
                SolanaValue(
                    listOf(
                        SolanaTokenAccount(
                            pubkey = tokenAddress ?: "",
                            account = SolanaAccount(
                                lamports = 100000,
                                space = 10,
                                owner = owner,
                                data = SolanaAccountParsed(
                                    SolanaAccountParsedInfo(
                                        SolanaTokenInfo(
                                            SolanaTokenAmount(((index + 1) * 10000000).toString())
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    override suspend fun getAccountInfoSpl(request: JSONRpcRequest<List<Any>>): JSONRpcResponse<SolanaValue<SolanaParsedData<SolanaInfo<SolanaParsedSplTokenInfo>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountInfoMpl(request: JSONRpcRequest<List<Any>>): JSONRpcResponse<SolanaValue<SolanaArrayData<String>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTokenInfo(request: JSONRpcRequest<List<Any>>): JSONRpcResponse<SolanaValue<SolanaParsedSplTokenInfo>> {
        TODO("Not yet implemented")
    }
}