package com.gemwallet.android.blockchain.clients.bitcoin

import com.wallet.core.blockchain.bitcoin.models.BitcoinAccount
import com.wallet.core.blockchain.bitcoin.models.BitcoinBlock
import com.wallet.core.blockchain.bitcoin.models.BitcoinFeeResult
import com.wallet.core.blockchain.bitcoin.models.BitcoinNodeInfo
import com.wallet.core.blockchain.bitcoin.models.BitcoinTransaction
import com.wallet.core.blockchain.bitcoin.models.BitcoinTransactionBroacastResult
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface BitcoinRpcClient {
    @GET("/api/v2/address/{address}")
    suspend fun getBalance(@Path("address") address: String): Result<BitcoinAccount>

    @GET("/api/v2/utxo/{address}")
    suspend fun getUTXO(@Path("address") address: String): Result<List<BitcoinUTXO>>

    @GET("/api/v2/estimatefee/{priority}")
    suspend fun estimateFee(@Path("priority") priority: String): Result<BitcoinFeeResult>

    @POST("/api/v2/sendtx/")
    suspend fun broadcast(@Body body: RequestBody): Result<BitcoinTransactionBroacastResult>

    @GET("/api/v2/tx/{txId}")
    suspend fun transaction(@Path("txId") txId: String): Result<BitcoinTransaction>

    @GET//("/api/v2/")
    suspend fun nodeInfo(@Url url: String): Result<BitcoinNodeInfo>

    @GET//("/api/v2/block/{block}")
    suspend fun block(@Url url: String): Response<BitcoinBlock>
}

suspend fun BitcoinRpcClient.getBlock(url: String): Response<BitcoinBlock> {
    return block("$url/api/v2/block/1")
}

suspend fun BitcoinRpcClient.getNodeInfo(url: String): Result<BitcoinNodeInfo> {
    return nodeInfo("$url/api/v2")
}