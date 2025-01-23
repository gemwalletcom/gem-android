package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

class SignClientProxy(
    private val clients: List<SignClient>
) : SignClient {

    override suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return clients.getClient(chain)?.signMessage(chain, input, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    override suspend fun signTypedMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return clients.getClient(chain)?.signTypedMessage(chain, input, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    override suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chain = params.input.assetId.chain
        return clients.getClient(chain)?.signTransaction(params, txSpeed, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }

}