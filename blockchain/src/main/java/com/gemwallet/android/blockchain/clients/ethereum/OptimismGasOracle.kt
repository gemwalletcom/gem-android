package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ethereum
import java.math.BigInteger

class OptimismGasOracle(
    private val callService: EvmCallService,
    private val coinType: CoinType,
) {

    suspend fun estimate(
        params: ConfirmParams,
        chainId: String,
        nonce: BigInteger,
        baseFee: BigInteger,
        priorityFee: BigInteger,
        gasLimit: BigInteger,
        txSpeed: FeePriority,
    ): GasFee = withContext(Dispatchers.IO) {
        val assetId = params.assetId
        val feeAssetId = AssetId(assetId.chain)
        val gasPrice = baseFee + priorityFee
        val minerFee = when (params.getTxType()) {
            TransactionType.Transfer -> if (assetId.type() == AssetSubtype.NATIVE && params.isMax()) gasPrice else priorityFee
            TransactionType.Swap, TransactionType.TokenApproval -> priorityFee
            else -> throw IllegalAccessException("Operation doesn't available")
        }
        val amount = when (params.getTxType()) {
            TransactionType.Transfer -> if (params.isMax()) params.amount - gasLimit * gasPrice else params.amount
            TransactionType.TokenApproval, TransactionType.Swap -> params.amount
            else -> throw IllegalAccessException("Operation doesn't available")
        }
        val encoded = encode(
            assetId = assetId,
            coin = coinType,
            amount = amount,
            destinationAddress = when (params) {
                is ConfirmParams.SwapParams -> params.to
                is ConfirmParams.TokenApprovalParams -> params.contract
                is ConfirmParams.TransferParams -> params.destination.address
                else -> throw IllegalArgumentException()
            },
            meta = when (params) {
                is ConfirmParams.SwapParams -> params.swapData
                is ConfirmParams.TokenApprovalParams -> params.data
                else -> params.memo()
            },
            chainId = chainId,
            nonce = nonce,
            gasFee = GasFee(
                feeAssetId = feeAssetId,
                priority = FeePriority.Normal,
                limit = gasLimit,
                maxGasPrice = gasPrice,
                minerFee = minerFee,
            ),
        )
        val l2Fee = gasPrice * gasLimit
        val l1Fee = getL1Fee(encoded) ?: throw IllegalStateException("Can't get L1 Fee")
        GasFee(
            feeAssetId = feeAssetId,
            priority = txSpeed,
            maxGasPrice = gasPrice,
            minerFee = minerFee,
            limit = gasLimit,
            amount = l1Fee + l2Fee,
        )
    }

    private suspend fun getL1Fee(data: ByteArray): BigInteger? {
        val abiFn = EthereumAbiFunction("getL1Fee").apply {
            this.addParamBytes(data, false)
        }
        val encodedFn = EthereumAbi.encode(abiFn)
        val request = JSONRpcRequest.create(
            EvmMethod.Call,
            listOf(
                mapOf(
                    "to" to "0x420000000000000000000000000000000000000F",
                    "data" to encodedFn.toHexString(),
                ),
                "latest",
            )
        )
        return callService.callNumber(request).getOrNull()?.result?.value
    }

    private fun encode(
        assetId: AssetId,
        coin: CoinType,
        destinationAddress: String,
        amount: BigInteger,
        meta: String?,
        chainId: String,
        nonce: BigInteger,
        gasFee: GasFee,
    ): ByteArray {
        val signInput = EvmSignClient(assetId.chain).buildSignInput(
            assetId = assetId,
            amount = amount,
            tokenAmount = amount,
            fee = gasFee,
            chainId = chainId.toBigInteger(),
            nonce = nonce,
            destinationAddress = destinationAddress,
            memo = meta,
            privateKey = PrivateKey().data(),
        )
        val signer = AnySigner.sign(signInput, coin, Ethereum.SigningOutput.parser())
        val signatureLenInRlp = 67 // 1 + 32 + 1 + 32 + 1
        val encoded = signer.encoded.toList().dropLast(signatureLenInRlp).toMutableList()
        when (assetId.type()) {
            AssetSubtype.NATIVE -> encoded.removeAt(2)
            else -> {}
        }
        return encoded.toByteArray()
    }
}