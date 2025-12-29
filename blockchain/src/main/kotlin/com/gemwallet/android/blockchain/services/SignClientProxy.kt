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
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import uniffi.gemstone.GemSwapQuoteDataType
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
            is ConfirmParams.PerpetualParams.Open -> client.signPerpetualOpen(input, chainData, params.finalAmount, fee, privateKey)
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

        return when (params.dataType) {
            GemSwapQuoteDataType.CONTRACT -> client.signSwap(params, chainData, finalAmount, fee, privateKey)
            GemSwapQuoteDataType.TRANSFER -> signSwapTransfer(
                params,
                chainData,
                fee,
                client,
                privateKey,
            )
        }
    }

    private suspend fun signSwapTransfer(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        fee: Fee,
        client: SignClient,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val memo = params.memo()
        val destinationAddress = params.toAddress//getSwapDestinationAddress(params)
        val transferParams = ConfirmParams.Builder(params.fromAsset, params.from, params.fromAmount, params.useMaxAmount)
            .transfer(
                destination = DestinationAddress(destinationAddress),
                memo = memo,
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
}