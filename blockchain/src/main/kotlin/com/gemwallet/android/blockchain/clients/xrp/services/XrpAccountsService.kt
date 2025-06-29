package com.gemwallet.android.blockchain.clients.xrp.services

import com.gemwallet.android.blockchain.clients.xrp.XrpMethod
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.blockchain.xrp.XRPAccountLinesResult
import com.wallet.core.blockchain.xrp.XRPAccountResult
import com.wallet.core.blockchain.xrp.XRPResult
import retrofit2.http.Body
import retrofit2.http.POST

interface XrpAccountsService {
    @POST("/")
    suspend fun account(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountResult>>

    @POST("/")
    suspend fun accountLines(@Body request: JSONRpcRequest<List<Map<String, String>>>): Result<XRPResult<XRPAccountLinesResult>>
}

internal suspend fun XrpAccountsService.account(address: String): Result<XRPResult<XRPAccountResult>> {
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

internal suspend fun XrpAccountsService.accountLines(address: String): Result<XRPResult<XRPAccountLinesResult>> {
    val request = JSONRpcRequest.create(
        XrpMethod.AccountLines,
        listOf(
            mapOf(
                "account" to address,
                "ledger_index" to "current",
            )
        )
    )
    return accountLines(request)
}