package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.XRPAccountResult
import com.wallet.core.blockchain.xrp.XRPFee
import com.wallet.core.blockchain.xrp.XRPLatestBlock
import com.wallet.core.blockchain.xrp.XRPResult
import com.wallet.core.blockchain.xrp.XRPTransactionBroadcast
import com.wallet.core.blockchain.xrp.XRPTransactionStatus
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface XrpRpcClient {

    @POST("/")
    suspend fun fee(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPFee>>

    @POST("/")
    suspend fun account(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountResult>>
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