package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaEpoch
import com.wallet.core.blockchain.solana.models.SolanaStakeAccount
import com.wallet.core.blockchain.solana.models.SolanaTokenAccountResult
import com.wallet.core.blockchain.solana.models.SolanaValidator
import com.wallet.core.blockchain.solana.models.SolanaValidators
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaStakeService {
    @POST("/")
    suspend fun validators(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValidators>>

    @POST("/")
    suspend fun delegations(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<List<SolanaTokenAccountResult<SolanaStakeAccount>>>>

    @POST("/")
    suspend fun epoch(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaEpoch>>
}

suspend fun SolanaStakeService.delegations(
    owner: String,
): List<SolanaTokenAccountResult<SolanaStakeAccount>>? {
    val request = JSONRpcRequest.create(
        SolanaMethod.GetDelegations,
        listOf(
            "Stake11111111111111111111111111111111111111",
            mapOf(
                "encoding" to "jsonParsed",
                "commitment" to "finalized",
                "filters" to listOf(
                    mapOf(
                        "memcmp" to mapOf(
                            "bytes" to owner,
                            "offset" to 44,
                        )
                    )
                )
            )
        )
    )
    return delegations(request).getOrNull()?.result
}

suspend fun SolanaStakeService.getDelegationsBalance(owner: String): Long {
    return delegations(owner)?.map { it.account.lamports }?.fold(0L) { acc, v -> acc + v } ?: 0L
}

suspend fun SolanaStakeService.validators(): List<SolanaValidator>? {
    val request = JSONRpcRequest.create(
        SolanaMethod.GetValidators,
        listOf<Any>(
            mapOf(
                "commitment" to "finalized",
                "keepUnstakedDelinquents" to false
            )
        )
    )
    return validators(request).getOrNull()?.result?.current
}

suspend fun SolanaStakeService.epoch(): SolanaEpoch? {
    return epoch(JSONRpcRequest.create(SolanaMethod.GetEpoch, emptyList())).getOrNull()?.result
}