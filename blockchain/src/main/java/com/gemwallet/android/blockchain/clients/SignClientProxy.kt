package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
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
        val client = clients.getClient(chain) ?: throw Exception("Chain isn't support")
        return try {
            client.signTransaction(params, txSpeed, privateKey)
        } catch (_: Throwable) {
            val input = params.input
            when (input) {
                is ConfirmParams.Stake.DelegateParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.Stake.RedelegateParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.Stake.RewardsParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.Stake.UndelegateParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.Stake.WithdrawParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.SwapParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.TokenApprovalParams -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.TransferParams.Native -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
                is ConfirmParams.TransferParams.Token -> client.sign(input, params.chainData, params.finalAmount, txSpeed, privateKey)
            }
        }
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }

}