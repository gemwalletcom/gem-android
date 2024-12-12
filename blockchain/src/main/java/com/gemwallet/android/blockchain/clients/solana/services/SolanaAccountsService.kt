package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.clients.solana.SolanaRpcClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaValue
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaAccountsService {
    @POST("/")
    suspend fun getTokenAccountByOwner(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>
}

suspend fun SolanaAccountsService.getTokenAccountByOwner(owner: String, tokenId: String): String? {
    val accountRequest = JSONRpcRequest.create(
        method = SolanaMethod.GetTokenAccountByOwner,
        params = listOf(
            owner,
            mapOf("mint" to tokenId),
            mapOf("encoding" to "jsonParsed"),
        )
    )
    return getTokenAccountByOwner(accountRequest).getOrNull()?.result?.value?.firstOrNull()?.pubkey
}