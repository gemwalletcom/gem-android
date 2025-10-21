package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.DestinationAddress
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import uniffi.gemstone.SwapperProvider
import java.math.BigInteger

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
        privateKey: ByteArray,
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
            is ConfirmParams.Stake.Freeze -> client.signFreeze(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.Stake.Unfreeze -> client.signUnfreeze(input, chainData, params.finalAmount, fee, privateKey)
            is ConfirmParams.SwapParams -> signSwap(params.input as ConfirmParams.SwapParams, chainData, params.finalAmount, fee, privateKey, client)
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

    private suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray,
        client: SignClient,
    ): List<ByteArray> {

        return when (params.providerId) {
            SwapperProvider.NEAR_INTENTS -> signSwapTransfer(
                params,
                chainData,
                fee,
                client,
                privateKey,
            )
            else -> client.signSwap(params, chainData, finalAmount, fee, privateKey)
        }
    }

    private suspend fun signSwapTransfer(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        fee: Fee,
        client: SignClient,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val memo = getSwapMemo(params.fromAsset, params.swapData)
        val destinationAddress = getSwapDestinationAddress(params)
        val transferParams = ConfirmParams.Builder(params.fromAsset, params.from, params.fromAmount)
            .transfer(
                destination = DestinationAddress(destinationAddress),
                memo = memo,
                isMax = params.isMax()
            )
        return when (transferParams) {
            is ConfirmParams.TransferParams.Native -> client.signNativeTransfer(
                    transferParams,
                    chainData,
                    params.fromAmount,
                    fee,
                    privateKey,
                )
            is ConfirmParams.TransferParams.Token -> client.signTokenTransfer(
                transferParams,
                chainData,
                params.fromAmount,
                fee,
                privateKey,
            )
            else -> throw IllegalArgumentException("Not sound signer")
        }
    }

    private fun getSwapMemo(from: Asset, swapData: String): String? {
        return when (from.chain) {
            Chain.Stellar -> swapData
            else -> null
        }
    }

    private fun getSwapDestinationAddress(params: ConfirmParams.SwapParams): String {
        if (params.fromAsset.id.tokenId == null) {
            return params.toAddress
        }
        return when (params.fromAsset.chain) {
            Chain.Ethereum,
            Chain.Tron -> {
                val callData = params.swapData.decodeHex()
                if (callData.size == 68) {
                    val addressData = byteArrayOf(20) + callData.sliceArray(4 ..< 36)
                    addressData.toHexString()
                } else {
                    throw IllegalArgumentException("Invalid call data")
                }
            }
            else -> return params.toAddress
        }
    }

//    func transferSwapInput(input: SignerInput, fromAsset: Asset, swapData: SwapData) throws -> SignerInput {
//        let memo = getMemo(fromAsset: fromAsset, swapData: swapData)
//        let destinationAddress = try getDestinationAddress(fromAsset: fromAsset, swapData: swapData)
//
//        return SignerInput(
//            type: .transfer(fromAsset),
//            asset: fromAsset,
//            value: swapData.quote.fromValueBigInt,
//            fee: input.fee,
//            isMaxAmount: input.useMaxAmount,
//            memo: memo,
//            senderAddress: input.senderAddress,
//            destinationAddress: destinationAddress,
//            metadata: input.metadata
//        )
//    }
}