package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.solana.models.SolanaArrayData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaInfo
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedData
import com.gemwallet.android.blockchain.clients.solana.models.SolanaParsedSplTokenInfo
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.blockchain.rpc.model.JSONRpcResponse
import com.wallet.core.blockchain.solana.models.SolanaBalance
import com.wallet.core.blockchain.solana.models.SolanaBalanceValue
import com.wallet.core.blockchain.solana.models.SolanaBlockhashResult
import com.wallet.core.blockchain.solana.models.SolanaEpoch
import com.wallet.core.blockchain.solana.models.SolanaPrioritizationFee
import com.wallet.core.blockchain.solana.models.SolanaStakeAccount
import com.wallet.core.blockchain.solana.models.SolanaTokenAccount
import com.wallet.core.blockchain.solana.models.SolanaTokenAccountResult
import com.wallet.core.blockchain.solana.models.SolanaTransaction
import com.wallet.core.blockchain.solana.models.SolanaValidators
import com.wallet.core.blockchain.solana.models.SolanaValue
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface SolanaRpcClient {
    @POST("/")
    suspend fun getBalance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaBalance>>

    @POST("/")
    suspend fun getTokenAccountByOwner(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>>

    @POST("/")
    suspend fun getAccountInfoSpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaParsedData<SolanaInfo<SolanaParsedSplTokenInfo>>>>>

    @POST("/")
    suspend fun getTokenInfo(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaTokenOwner>>>

    @POST("/")
    suspend fun getAccountInfoMpl(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValue<SolanaArrayData<String>>>>

    @POST("/")
    suspend fun getTokenBalance(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaValue<SolanaBalanceValue>>>

    @POST("/")
    suspend fun rentExemption(@Body request: JSONRpcRequest<List<Int>>): Result<JSONRpcResponse<Int>>

    @POST("/")
    suspend fun getBlockhash(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaBlockhashResult>>

    @POST("/")
    suspend fun getPriorityFees(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<List<SolanaPrioritizationFee>>>

    @POST("/")
    suspend fun broadcast(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<String>>

    @POST("/")
    suspend fun transaction(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaTransaction>>

    @POST("/")
    suspend fun validators(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<SolanaValidators>>

    @POST("/")
    suspend fun delegations(@Body request: JSONRpcRequest<List<Any>>): Result<JSONRpcResponse<List<SolanaTokenAccountResult<SolanaStakeAccount>>>>

    @POST("/")
    suspend fun epoch(@Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<SolanaEpoch>>

    @POST
    suspend fun health(@Url url: String,@Body request: JSONRpcRequest<List<String>>): Response<JSONRpcResponse<String>>

    @POST
    suspend fun slot(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<Int>>

    @POST
    suspend fun genesisHash(@Url url: String, @Body request: JSONRpcRequest<List<String>>): Result<JSONRpcResponse<String>>
}

class SolanaTokenOwner(val owner: String)

suspend fun SolanaRpcClient.getTokenAccountByOwner(
    owner: String,
    tokenId: String,
): Result<JSONRpcResponse<SolanaValue<List<SolanaTokenAccount>>>> {
    val accountRequest = JSONRpcRequest.create(
        method = SolanaMethod.GetTokenAccountByOwner,
        params = listOf(
            owner,
            mapOf("mint" to tokenId),
            mapOf("encoding" to "jsonParsed"),
        )
    )
    return getTokenAccountByOwner(accountRequest)
}

suspend fun SolanaRpcClient.delegations(
    owner: String,
): Result<JSONRpcResponse<List<SolanaTokenAccountResult<SolanaStakeAccount>>>> {
    val request = JSONRpcRequest.create(
        SolanaMethod.GetDelegations,
        listOf(
            "Stake11111111111111111111111111111111111111",
            mapOf(
                "encoding" to "jsonParsed",
                "commitment" to "finalized",
                "filters" to listOf(
                    mapOf(
                        "memcmp" to mapOf(
                            "bytes" to owner,
                            "offset" to 44,
                        )
                    )
                )
            )
        )
    )
    return delegations(request)
}

suspend fun SolanaRpcClient.getPriorityFees(): List<SolanaPrioritizationFee> {
    val request = JSONRpcRequest.create(SolanaMethod.GetPriorityFee, listOf<String>())
    return getPriorityFees(request).getOrNull()?.result ?: throw Exception()
}

suspend fun SolanaRpcClient.health(url: String): Response<JSONRpcResponse<String>> {
    return health(url, JSONRpcRequest.create(SolanaMethod.GetHealth, emptyList()))
}

suspend fun SolanaRpcClient.slot(url: String): Result<JSONRpcResponse<Int>> {
    return slot(url, JSONRpcRequest.create(SolanaMethod.GetSlot, emptyList()))
}

suspend fun SolanaRpcClient.genesisHash(url: String): Result<JSONRpcResponse<String>> {
    return genesisHash(url, JSONRpcRequest.create(SolanaMethod.GetGenesisHash, emptyList()))
}