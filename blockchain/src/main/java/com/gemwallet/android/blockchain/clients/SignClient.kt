package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

interface SignClient : BlockchainClient {

    suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray,
    ): ByteArray = byteArrayOf()

    suspend fun signTypedMessage(chain: Chain, input: ByteArray, privateKey: ByteArray): ByteArray = byteArrayOf()

    suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed = TxSpeed.Normal,
        privateKey: ByteArray,
    ): ByteArray
}