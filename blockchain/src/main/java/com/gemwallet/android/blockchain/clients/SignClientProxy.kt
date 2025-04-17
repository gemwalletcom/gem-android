package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority

class SignClientProxy(
    private val clients: List<SignClient>
) {

    suspend fun signMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return clients.getClient(chain)?.signMessage(chain, input, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    suspend fun signTypedMessage(
        chain: Chain,
        input: ByteArray,
        privateKey: ByteArray
    ): ByteArray {
        return clients.getClient(chain)?.signTypedMessage(chain, input, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    suspend fun signData(
        chain: Chain,
        input: String,
        privateKey: ByteArray
    ): ByteArray {
        return clients.getClient(chain)?.signData(chain, input, privateKey)
            ?: throw Exception("Chain isn't support")
    }

    suspend fun signTransaction(
        params: SignerParams,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chain = params.input.asset.id.chain
        val client = clients.getClient(chain) ?: throw Exception("Chain isn't support")
        val input = params.input
        return when (input) {
            is ConfirmParams.Stake.DelegateParams -> client.signDelegate(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Stake.RedelegateParams -> client.signRedelegate(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Stake.RewardsParams -> client.signRewards(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Stake.UndelegateParams -> client.signUndelegate(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Stake.WithdrawParams -> client.signWithdraw(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.SwapParams -> client.signSwap(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TokenApprovalParams -> client.signTokenApproval(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TransferParams.Generic -> client.signGenericTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TransferParams.Native -> client.signNativeTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.TransferParams.Token -> client.signTokenTransfer(input, params.chainData, params.finalAmount, feePriority, privateKey)
            is ConfirmParams.Activate -> client.signActivate(input, params.chainData, params.finalAmount, feePriority, privateKey)
        }
    }

    fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}