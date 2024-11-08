package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed

interface SignClient : BlockchainClient {

    suspend fun signMessage(
        input: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = byteArrayOf()

    suspend fun signTypedMessage(input: ByteArray, privateKey: ByteArray): ByteArray = byteArrayOf()

    suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed = TxSpeed.Normal,
        privateKey: ByteArray,
    ): ByteArray
}