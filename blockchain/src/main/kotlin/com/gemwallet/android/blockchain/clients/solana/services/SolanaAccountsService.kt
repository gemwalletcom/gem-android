package com.gemwallet.android.blockchain.clients.solana.services

import com.gemwallet.android.blockchain.clients.solana.SolanaMethod
import com.gemwallet.android.blockchain.clients.solana.models.SolanaArrayData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedSplTokenInfo
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.SolanaTokenAccount
import com.wallet.core.blockchain.solana.SolanaValue
import retrofit2.http.Body
import retrofit2.http.POST

interface SolanaAccountsService {
    @POST("/")
    suspend fun getTokenAccountByOwner(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>

    @POST("/")
    suspend fun batchAccount(@Body request: List<JSONRpcRequest<List<Any>>>): Result<List<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>>

    @POST("/")
    suspend fun getAccountInfoSpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaParsedData<SolanaInfo<SolanaParsedSplTokenInfo>>>>>

    @POST("/")
    suspend fun getAccountInfoMpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaArrayData<String>>>>

    @POST("/")
    suspend fun getTokenInfo(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaParsedSplTokenInfo>>>
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

fun SolanaAccountsService.createAccountByOwnerRequest(owner: String, tokenId: String): JSONRpcRequest<List<Any>> {
    return JSONRpcRequest.create(
        method = SolanaMethod.GetTokenAccountByOwner,
        params = listOf(
            owner,
            mapOf("mint" to tokenId),
            mapOf(
                "encoding" to "jsonParsed",
                SolanaRpcClient.commitmentKey to SolanaRpcClient.commitmentValue,
            ),
        ),
    )
}
