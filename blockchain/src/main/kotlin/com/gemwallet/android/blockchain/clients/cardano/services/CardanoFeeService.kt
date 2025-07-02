package com.gemwallet.android.blockchain.clients.cardano.services

import com.wallet.core.blockchain.cardano.CardanoUTXO
import com.wallet.core.blockchain.cardano.CardanoUTXOS
import com.wallet.core.primitives.GraphqlData
import com.wallet.core.primitives.GraphqlRequest
import com.wallet.core.primitives.UTXO
import retrofit2.http.Body
import retrofit2.http.POST

interface CardanoFeeService {
    @POST("/")
    suspend fun utxos(@Body request: GraphqlRequest): Result<GraphqlData<CardanoUTXOS<List<CardanoUTXO>>>>
}

suspend fun CardanoFeeService.utxos(address: String): List<UTXO> {
    val request = GraphqlRequest(
            operationName = "UtxoSetForAddress",
        variables = hashMapOf("address" to address),
        query = "query UtxoSetForAddress(\$address: String!) { utxos(order_by: { value: desc } , where: { address: { _eq: \$address }  } ) { address value txHash index tokens { quantity asset { fingerprint policyId assetName } } } }"
    )
    return utxos(request).getOrNull()?.data?.utxos?.map {
        UTXO(
            transaction_id = it.txHash,
            vout = it.index,
            value = it.value,
            address = it.address,
        )
    } ?: emptyList()
}