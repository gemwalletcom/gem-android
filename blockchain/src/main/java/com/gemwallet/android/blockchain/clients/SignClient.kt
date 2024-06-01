package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.SignerParams

interface SignClient : BlockchainClient {

    suspend fun signMessage(
        input: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = byteArrayOf()

    suspend fun signTransfer(
        params: SignerParams,
        privateKey: ByteArray,
    ): ByteArray
}