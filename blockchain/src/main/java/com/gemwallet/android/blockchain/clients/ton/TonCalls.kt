package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest

internal suspend fun jettonAddress(rpcClient: TonRpcClient, tokenId: String, address: String): String? {
    val data = uniffi.gemstone.tonEncodeGetWalletAddress(address)
    val response = rpcClient.getJetonAddress(
        JSONRpcRequest(
            method = "runGetMethod",
            params = mapOf(
                "address" to tokenId,
                "method" to "get_wallet_address",
                "stack" to listOf(
                    listOf(
                        "tvm.Slice",
                        data,
                    )
                ),
            )
        )
    )
    val result = response.getOrNull() ?: return null
    return uniffi.gemstone.tonDecodeJettonAddress(result.b64, result.len.toULong())
}