package com.gemwallet.android.blockchain.clients.tron.services

import com.wallet.core.blockchain.tron.models.TronSmartContractResult
import retrofit2.http.Body
import retrofit2.http.POST

interface TronCallService {
    @POST("/wallet/triggerconstantcontract")
    suspend fun triggerSmartContract(@Body addressRequest: Any): Result<TronSmartContractResult>
}

suspend fun TronCallService.triggerSmartContract(
    contractAddress: String,
    functionSelector: String,
    parameter: String? = null,
    feeLimit: Long? = null,
    callValue: Long? = null,
    ownerAddress: String,
    visible: Boolean? = null,
): Result<TronSmartContractResult> {
    val call = mapOf(
        "contract_address" to contractAddress,
        "function_selector" to functionSelector,
        "parameter" to parameter,
        "fee_limit" to feeLimit,
        "call_value" to callValue,
        "owner_address" to ownerAddress,
        "visible" to visible,
    )
    return triggerSmartContract(call)
}