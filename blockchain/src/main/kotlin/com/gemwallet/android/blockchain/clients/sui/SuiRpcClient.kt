package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.sui.model.SuiObject
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.sui.SuiBroadcastTransaction
import com.wallet.core.blockchain.sui.SuiCoin
import com.wallet.core.blockchain.sui.SuiCoinBalance
import com.wallet.core.blockchain.sui.SuiCoinMetadata
import com.wallet.core.blockchain.sui.SuiData
import com.wallet.core.blockchain.sui.SuiGasUsed
import com.wallet.core.blockchain.sui.SuiStakeDelegation
import com.wallet.core.blockchain.sui.SuiSystemState
import com.wallet.core.blockchain.sui.SuiTransaction
import com.wallet.core.blockchain.sui.SuiValidators
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SuiRpcClient {

    @POST("/")
    suspend fun coins(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiData<List<SuiCoin>>>>

    @POST("/")
    suspend fun gasPrice(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>

    @POST("/")
    suspend fun dryRun(@Body request: JSONRpcRequest<List<String>>):Result<JSONRpcResponse<SuiTransaction>>

    @POST("/")
    suspend fun getObject(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SuiObject>>
}

internal suspend fun SuiRpcClient.coins(address: String, coinType: String): Result<JSONRpcResponse<SuiData<List<SuiCoin>>>> {
    return coins(JSONRpcRequest.create(SuiMethod.Coins, listOf(address, coinType)))
}

internal suspend fun SuiRpcClient.dryRun(data: String): SuiGasUsed {
    return dryRun(JSONRpcRequest.create(SuiMethod.DryRun, listOf(data))).getOrNull()?.result?.effects?.gasUsed
        ?: throw Exception("Can't load SUI gas")
}