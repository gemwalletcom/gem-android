package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.models.XRPAccountResult
import com.wallet.core.blockchain.xrp.models.XRPFee
import com.wallet.core.blockchain.xrp.models.XRPLatestBlock
import com.wallet.core.blockchain.xrp.models.XRPResult
import com.wallet.core.blockchain.xrp.models.XRPTransactionBroadcast
import com.wallet.core.blockchain.xrp.models.XRPTransactionStatus
import retrofit2.http.Body
import retrofit2.http.POST

interface XrpRpcClient {
    @POST("/")
    suspend fun account(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountResult>>

    @POST("/")
    suspend fun fee(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPFee>>

    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPTransactionStatus>>

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPTransactionBroadcast>>

    @POST("/")
    suspend fun latestBlock(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPLatestBlock>>
}

internal suspend fun XrpRpcClient.account(address: String): Result<XRPResult<XRPAccountResult>> {
    val request = JSONRpcRequest.create(
        XrpMethod.Account,
        listOf(
            mapOf(
                "account" to address,
                "ledger_index" to "current",
            )
        )
    )
    return account(request)
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

internal suspend fun XrpRpcClient.latestBlock(): Result<XRPResult<XRPLatestBlock>> {
    return latestBlock(JSONRpcRequest.create(XrpMethod.LatestBlock, emptyList()))
}