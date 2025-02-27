package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.models.SolanaArrayData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedSplTokenInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaTokenOwner
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaAccount
import com.wallet.core.blockchain.solana.models.SolanaAccountParsed
import com.wallet.core.blockchain.solana.models.SolanaAccountParsedInfo
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaTokenAmount
import com.wallet.core.blockchain.solana.models.SolanaTokenInfo
import com.wallet.core.blockchain.solana.models.SolanaValue

internal class TestSolanaAccountsService(
    private val tokenAddress: String? = null,
) : SolanaAccountsService {
    var tokenAccountRequest: JSONRpcRequest<List<Any>>? = null

    override suspend fun getTokenAccountByOwner(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>> {
        tokenAccountRequest = request
        return Result.success(
            JSONRpcResponse(
                SolanaValue(
                    listOf(
//                        SolanaAccountParsed<SolanaAccountParsedInfo<SolanaTokenInfo>>
                        SolanaTokenAccount(
                            pubkey = "",
                            account = SolanaAccount(
                                lamports = 100000,
                                space = 10,
                                owner = "",
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
        )
    }

    override suspend fun batchAccount(request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountInfoSpl(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaParsedData<SolanaInfo<SolanaParsedSplTokenInfo>>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountInfoMpl(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaArrayData<String>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTokenInfo(request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaTokenOwner>>> {
        TODO("Not yet implemented")
    }
}