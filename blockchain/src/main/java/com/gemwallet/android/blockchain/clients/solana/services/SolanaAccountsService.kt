package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.clients.solana.models.SolanaArrayData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedSplTokenInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaTokenOwner
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaBalanceValue
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaValue
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaAccountsService {
    @POST("/")
    suspend fun getTokenAccountByOwner(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>

    @POST("/")
    suspend fun batchAccount(@Body request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>>

    @POST("/")
    suspend fun batchBalances(@Body request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<SolanaBalanceValue>>>>

    @POST("/")
    suspend fun getAccountInfoSpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaParsedData<SolanaInfo<SolanaParsedSplTokenInfo>>>>>

    @POST("/")
    suspend fun getAccountInfoMpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaArrayData<String>>>>

    @POST("/")
    suspend fun getTokenInfo(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaTokenOwner>>>
}

suspend fun SolanaAccountsService.getTokenAccountByOwner(owner: String, tokenId: String): String? {
    val accountRequest = JSONRpcRequest.create(
        method = SolanaMethod.GetTokenAccountByOwner,
        params = listOf(
            owner,
            mapOf("mint" to tokenId),
            mapOf(
                "encoding" to "jsonParsed",
                SolanaRpcClient.commitmentKey to SolanaRpcClient.commitmentValue,
            ),
        )
    )
    return getTokenAccountByOwner(accountRequest).getOrNull()?.result?.value?.firstOrNull()?.pubkey
}

suspend fun SolanaAccountsService.getTokenInfo(tokenId: String): String? {
    return getTokenInfo(createAccountInfoRequest(tokenId)).getOrNull()?.result?.value?.owner
}

fun SolanaAccountsService.createAccountInfoRequest(tokenId: String): JSONRpcRequest<List<Any>> {
    return JSONRpcRequest.create(
        SolanaMethod.GetAccountInfo,
        params = listOf(
            tokenId,
            mapOf(
                "encoding" to "jsonParsed",
                SolanaRpcClient.commitmentKey to SolanaRpcClient.commitmentValue,
            ),
        )
    )
}
