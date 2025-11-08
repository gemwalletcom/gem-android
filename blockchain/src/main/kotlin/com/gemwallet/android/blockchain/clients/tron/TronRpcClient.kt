package com.gemwallet.android.blockchain.clients.tron

import com.wallet.core.blockchain.tron.TronAccount
import com.wallet.core.blockchain.tron.TronAccountRequest
import com.wallet.core.blockchain.tron.TronAccountUsage
import com.wallet.core.blockchain.tron.TronBlock
import com.wallet.core.blockchain.tron.TronChainParameters
import com.wallet.core.blockchain.tron.TronSmartContractResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TronRpcClient {
    @GET("/wallet/getchainparameters")
    suspend fun getChainParameters(): Result<TronChainParameters>

    @POST("/wallet/getnowblock")
    suspend fun nowBlock(): Result<TronBlock>

    @POST("/wallet/triggerconstantcontract")
    suspend fun triggerSmartContract(@Body addressRequest: Any): Result<TronSmartContractResult>

    @POST("/wallet/getaccount")
    suspend fun getAccount(@Body addressRequest: TronAccountRequest): Result<TronAccount>

    @POST("/wallet/getaccountnet")
    suspend fun getAccountUsage(@Body addressRequest: TronAccountRequest): Result<TronAccountUsage>
}

suspend fun TronRpcClient.triggerSmartContract(
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

suspend fun TronRpcClient.getAccount(address: String, visible: Boolean = false): TronAccount? {
    return try {
        val result = getAccount(
            TronAccountRequest(
                address = address,
                visible = visible
            )
        )
        result.getOrThrow()
    } catch (err: Throwable) {
        null
    }
}