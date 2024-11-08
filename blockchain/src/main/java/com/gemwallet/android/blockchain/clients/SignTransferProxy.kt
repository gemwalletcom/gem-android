package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.blockchain.operators.SignTransfer
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

class SignTransferProxy(
    private val clients: List<SignClient>,
) : SignTransfer {

    override suspend fun invoke(
        input: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): Result<ByteArray> {
        return try {
            val result = clients.firstOrNull { it.isMaintain(input.input.assetId.chain) }?.signTransfer(
                params = input,
                txSpeed = txSpeed,
                privateKey = privateKey
            ) ?: throw Exception("Impossible sign transfer")
            Result.success(result)
        } catch (err: Throwable) {
            Result.failure(err)
        }
    }

    override suspend fun invoke(chain: Chain, input: ByteArray, privateKey: ByteArray, isTyped: Boolean): Result<ByteArray> {
        return try {
            val client = clients.firstOrNull { it.isMaintain(chain) } ?: throw Exception("Impossible sign transfer")
            val result = if (isTyped) client.signTypedMessage(input, privateKey) else client.signMessage(input, privateKey)
            Result.success(result)
        } catch (err: Throwable) {
            Result.failure(err)
        }
    }
}