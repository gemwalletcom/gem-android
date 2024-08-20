package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ethereum
import java.math.BigInteger

class OptimismGasOracle {

    suspend operator fun invoke(
        params: ConfirmParams,
        chainId: BigInteger,
        nonce: BigInteger,
        gasLimit: BigInteger,
        coin: CoinType,
        rpcClient: EvmRpcClient
    ): GasFee = withContext(Dispatchers.IO) {
        val assetId = params.assetId
        val feeAssetId = AssetId(assetId.chain)
        val basePriorityFee = async { EvmFee.getBasePriorityFee(rpcClient) }
        val (baseFee, priorityFee) = basePriorityFee.await()
        val gasPrice = baseFee + priorityFee
        val minerFee = when (params.getTxType()) {
            TransactionType.Transfer -> if (assetId.type() == AssetSubtype.NATIVE && params.isMax()) gasPrice else priorityFee
            TransactionType.Swap -> priorityFee
            else -> throw IllegalAccessException("Operation doesn't available")
        }
        val amount = when (params.getTxType()) {
            TransactionType.Transfer -> if (params.isMax()) params.amount - gasLimit * gasPrice else params.amount
            TransactionType.TokenApproval, TransactionType.Swap -> params.amount
            else -> throw IllegalAccessException("Operation doesn't available")
        }
        val encoded = encode(
            assetId = assetId,
            coin = coin,
            amount = amount,
            destinationAddress = when (params) {
                is ConfirmParams.SwapParams -> params.to
                is ConfirmParams.TokenApprovalParams -> params.assetId.tokenId!!
                is ConfirmParams.TransferParams -> params.destination.address
                else -> throw IllegalArgumentException()
            },
            meta = when (params) {
                is ConfirmParams.SwapParams -> params.swapData
                is ConfirmParams.TokenApprovalParams -> params.approvalData
                else -> params.memo()
            },
            chainId = chainId,
            nonce = nonce,
            gasFee = GasFee(
                feeAssetId = feeAssetId,
                speed = TxSpeed.Normal,
                limit = gasLimit,
                maxGasPrice = gasPrice,
                minerFee = minerFee,
            ),
        )
        val l2Fee = gasPrice * gasLimit
        val l1Fee = getL1Fee(encoded, rpcClient)!!
        GasFee(
            feeAssetId = feeAssetId,
            speed = TxSpeed.Normal,
            maxGasPrice = gasPrice,
            minerFee = minerFee,
            limit = gasLimit,
            amount = l1Fee + l2Fee,
        )
    }

    class BaseFeeRequest(val to: String, val data: String)

    private suspend fun getL1Fee(data: ByteArray, rpcClient: EvmRpcClient): BigInteger? {
        val abiFn = EthereumAbiFunction("getL1Fee").apply {
            this.addParamBytes(data, false)
        }
        val encodedFn = EthereumAbi.encode(abiFn)
        val request = JSONRpcRequest.create(
            EvmMethod.Call,
            listOf(
                BaseFeeRequest(
                    to = "0x420000000000000000000000000000000000000F", encodedFn.toHexString(),
                ),
                "latest",
            )
        )
        return rpcClient.callNumber(request).getOrNull()?.result?.value
    }

    private fun encode(
        assetId: AssetId,
        coin: CoinType,
        destinationAddress: String,
        amount: BigInteger,
        meta: String?,
        chainId: BigInteger,
        nonce: BigInteger,
        gasFee: GasFee,
    ): ByteArray {
        val signInput = EvmSignClient(assetId.chain).buildSignInput(
            assetId = assetId,
            amount = amount,
            tokenAmount = amount,
            fee = gasFee,
            chainId = chainId,
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