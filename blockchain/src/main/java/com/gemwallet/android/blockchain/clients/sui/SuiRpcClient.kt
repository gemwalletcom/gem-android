package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.ethereum.EvmRpcClient
import com.gemwallet.android.blockchain.clients.sui.model.SuiObject
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.sui.SuiBroadcastTransaction
import com.wallet.core.blockchain.sui.SuiCoin
import com.wallet.core.blockchain.sui.SuiCoinBalance
import com.wallet.core.blockchain.sui.SuiData
import com.wallet.core.blockchain.sui.SuiStakeDelegation
import com.wallet.core.blockchain.sui.SuiTransaction
import com.wallet.core.blockchain.sui.SuiValidators
import com.wallet.core.blockchain.sui.models.SuiCoinMetadata
import com.wallet.core.blockchain.sui.models.SuiSystemState
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SuiRpcClient {

    @POST("/")
    suspend fun coins(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiData<List<SuiCoin>>>>

    @POST("/")
    suspend fun balance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiCoinBalance>>

    @POST("/")
    suspend fun balances(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SuiCoinBalance>>>

    @POST("/")
    suspend fun gasPrice(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>

    @POST("/")
    suspend fun dryRun(@Body request: JSONRpcRequest<List<String>>):Result<JSONRpcResponse<SuiTransaction>>

    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SuiTransaction>>

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<Any?>>): Result<JSONRpcResponse<SuiBroadcastTransaction>>

    @POST("/")
    suspend fun validators(@Body request: JSONRpcRequest<List<Any?>>): Result<JSONRpcResponse<SuiValidators>>

    @POST("/")
    suspend fun delegations(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SuiStakeDelegation>>>

    @POST("/")
    suspend fun getObject(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiObject>>

    @POST("/")
    suspend fun getCoinMetadata(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiCoinMetadata>>

    @POST("/")
    suspend fun systemState(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiSystemState>>

    @POST
    suspend fun chainId(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Response<JSONRpcResponse<String>>

    @POST
    suspend fun latestBlock(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<EvmRpcClient.EvmNumber>>
}

internal suspend fun SuiRpcClient.coins(address: String, coinType: String): Result<JSONRpcResponse<SuiData<List<SuiCoin>>>> {
    return coins(JSONRpcRequest.create(SuiMethod.Coins, listOf(address, coinType)))
}

internal suspend fun SuiRpcClient.balance(address: String): Result<JSONRpcResponse<SuiCoinBalance>> {
    return balance(JSONRpcRequest.create(SuiMethod.Balance, listOf(address)))
}

internal suspend fun SuiRpcClient.balances(address: String): Result<JSONRpcResponse<List<SuiCoinBalance>>> {
    return balances(JSONRpcRequest.create(SuiMethod.Balances, listOf(address)))
}

internal suspend fun SuiRpcClient.broadcast(data: String, sign: String): Result<JSONRpcResponse<SuiBroadcastTransaction>> {
    val request = JSONRpcRequest.create(
        SuiMethod.Broadcast,
        listOf(
            data,
            listOf(sign),
            null,
            "WaitForLocalExecution",
        )
    )
    return broadcast(request)
}

internal suspend fun SuiRpcClient.transaction(txId: String): Result<JSONRpcResponse<SuiTransaction>> {
    val request = JSONRpcRequest.create(
        SuiMethod.Transaction,
        listOf(
            txId,
            mapOf("showEffects" to true)
        )
    )
    return transaction(request)
}

internal suspend fun SuiRpcClient.systemState(): Result<JSONRpcResponse<SuiSystemState>> {
    val request = JSONRpcRequest.create(
        SuiMethod.SystemState,
        emptyList<String>(),
    )
    return systemState(request)
}

internal suspend fun SuiRpcClient.chainId(url: String): Response<JSONRpcResponse<String>> {
    return chainId(url, JSONRpcRequest.create(SuiMethod.ChainId, emptyList()))
}

internal suspend fun SuiRpcClient.latestBlock(url: String): Result<JSONRpcResponse<EvmRpcClient.EvmNumber>> {
    return latestBlock(url, JSONRpcRequest.create(SuiMethod.LatestCheckpoint, emptyList()))
}