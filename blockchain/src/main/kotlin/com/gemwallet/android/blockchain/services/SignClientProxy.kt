package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.clients.getClient
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

    suspend fun signTransaction(
        params: SignerParams,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chain = params.input.asset.id.chain
        val client = clients.getClient(chain) ?: throw Exception("Chain isn't support")
        val input = params.input
        val data = params.data(feePriority)
        val fee = data.fee
        val chainData = data.chainData
        return when (input) {
            is ConfirmParams.Stake.DelegateParams -> client.signDelegate(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Stake.RedelegateParams -> client.signRedelegate(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Stake.RewardsParams -> client.signRewards(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Stake.UndelegateParams -> client.signUndelegate(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Stake.WithdrawParams -> client.signWithdraw(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.SwapParams -> client.signSwap(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.TokenApprovalParams -> client.signTokenApproval(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.TransferParams.Generic -> client.signGenericTransfer(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.TransferParams.Native -> client.signNativeTransfer(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.TransferParams.Token -> client.signTokenTransfer(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Activate -> client.signActivate(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.NftParams -> client.signNft(input, chainData, params.finalAmount, fee, privateKey)
        }
    }

    fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}