package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.XRPFee
import com.wallet.core.blockchain.xrp.XRPLatestBlock
import com.wallet.core.blockchain.xrp.XRPResult
import com.wallet.core.blockchain.xrp.XRPTransactionBroadcast
import com.wallet.core.blockchain.xrp.XRPTransactionStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface XrpRpcClient : XrpAccountsService {

    @POST("/")
    suspend fun fee(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPFee>>

    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPTransactionStatus>>

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPTransactionBroadcast>>

    @POST//("/")
    suspend fun latestBlock(@Url url: String, @Body request: JSONRpcRequest<List<Map<String, String>>>): Response<XRPResult<XRPLatestBlock>>
}

internal suspend fun XrpRpcClient.fee(): Result<XRPResult<XRPFee>> {
    return fee(JSONRpcRequest.create(XrpMethod.Fee, listOf(mapOf())))
}

internal suspend fun XrpRpcClient.transaction(txId: String): Result<XRPResult<XRPTransactionStatus>> {
    val request = JSONRpcRequest.create(
        XrpMethod.Transaction,
        listOf(
            mapOf(
                "transaction" to txId,
            )
        )
    )
    return transaction(request)
}

internal suspend fun XrpRpcClient.broadcast(data: String): Result<XRPResult<XRPTransactionBroadcast>> {
    val request = JSONRpcRequest.create(
        XrpMethod.Broadcast,
        listOf(
            mapOf(
                "tx_blob" to data,
                "fail_hard" to "true"
            )
        )
    )
    return broadcast(request)
}

internal suspend fun XrpRpcClient.latestBlock(url: String): Response<XRPResult<XRPLatestBlock>> {
    return latestBlock(url, JSONRpcRequest.create(XrpMethod.LatestBlock, emptyList()))
}