package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumMessageSigner
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ethereum
import wallet.core.jni.proto.Ethereum.Transaction.ContractGeneric
import java.math.BigInteger

class EvmSignClient(
    private val chain: Chain,
) : SignClient {
    val coinType = WCChainTypeProxy().invoke(chain)

    override suspend fun signMessage(chain: Chain, input: ByteArray, privateKey: ByteArray): ByteArray {
        val result = PrivateKey(privateKey).sign(input, CoinType.ETHEREUM.curve())
        result[64] = (result[64] + 27).toByte()
        return result
    }

    override suspend fun signTypedMessage(chain: Chain, input: ByteArray, privateKey: ByteArray): ByteArray {
        val privateKey = PrivateKey(privateKey)
        val json = String(input)
        return EthereumMessageSigner.signTypedMessage(privateKey, json).decodeHex()
    }

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val input = buildSignInput(
            assetId = params.assetId,
            amount = finalAmount,
            tokenAmount = finalAmount,
            fee = meta.gasFee(txSpeed),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination().address,
            memo = params.memo(),
            privateKey = privateKey,
        )
        return sign(input, privateKey)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger.ZERO
        val input = buildSignInput(
            assetId = params.assetId,
            amount = amount,
            tokenAmount = finalAmount,
            fee = meta.gasFee(txSpeed),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination().address,
            memo = params.memo(),
            privateKey = privateKey,
        )
        return sign(input, privateKey)
    }

    override suspend fun signTokenApproval(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger.ZERO
        val input = buildSignInput(
            assetId = params.assetId,
            amount = amount,
            tokenAmount = finalAmount,
            fee = meta.gasFee(txSpeed),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination()?.address ?: "",
            memo = params.data,
            privateKey = privateKey,
        )
        return sign(input, privateKey)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val approvalData = params.approval
        val chainData = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger(params.value)

        val approvalSign = if (approvalData != null) {
            signTokenApproval(
                params = ConfirmParams.Builder(params.assetId, params.from)
                    .approval(
                        approvalData = encodeApprove(approvalData.spender).toHexString(),
                        provider = "",
                        contract = approvalData.token,
                    ),
                chainData = chainData,
                finalAmount = BigInteger.ZERO,
                txSpeed = txSpeed,
                privateKey = privateKey
            )
        } else emptyList()

        val fee = chainData.gasFee(txSpeed).let {
            if (approvalData == null) {
                it
            } else {
                GasFee(
                    feeAssetId = it.feeAssetId,
                    speed = it.speed,
                    limit = params.gasLimit!!,
                    maxGasPrice = it.maxGasPrice,
                    minerFee = it.minerFee,
                    amount = params.gasLimit!!.multiply(it.maxGasPrice)
                )
            }
        }

        val swapInput = buildSignInput(
            assetId = AssetId(params.assetId.chain),
            amount = amount,
            tokenAmount = finalAmount,
            fee = fee,
            chainId = chainData.chainId.toBigInteger(),
            nonce = chainData.nonce + if (approvalSign.isEmpty()) BigInteger.ZERO else BigInteger.ONE,
            destinationAddress = params.destination().address,
            memo = params.swapData,
            privateKey = privateKey,
        )

        val swapSign = sign(swapInput, privateKey)
        return approvalSign + swapSign
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, txSpeed, privateKey)
    }

    override suspend fun signRedelegate(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, txSpeed, privateKey)
    }

    override suspend fun signRewards(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, txSpeed, privateKey)
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, txSpeed, privateKey)
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, txSpeed, privateKey)
    }

    private fun stakeSmartchain(
        params: ConfirmParams.Stake,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        if (params.assetId.chain != Chain.SmartChain) {
            throw Exception("Doesn't support")
        }
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val fee = meta.gasFee(txSpeed)
        val valueData = when (params) {
            is ConfirmParams.Stake.DelegateParams -> finalAmount.toByteArray()
            else -> BigInteger.ZERO.toByteArray()
        }
        val callData = StakeHub.encodeStake(params)
        val input = Ethereum.SigningInput.newBuilder().apply {
            when (chain.eip1559Support()) {
                true -> {
                    this.txMode = Ethereum.TransactionMode.Enveloped
                    this.maxFeePerGas = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
                    this.maxInclusionFeePerGas = ByteString.copyFrom(fee.minerFee.toByteArray())
                }
                false -> {
                    this.txMode = Ethereum.TransactionMode.Legacy
                    this.gasPrice = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
                }
            }
            this.gasLimit = ByteString.copyFrom(fee.limit.toByteArray())
            this.chainId = ByteString.copyFrom(meta.chainId.toByteArray())
            this.nonce = ByteString.copyFrom(meta.nonce.toByteArray())
            this.toAddress = StakeHub.address
            this.privateKey = ByteString.copyFrom(privateKey)
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                contractGeneric = ContractGeneric.newBuilder().apply {
                    amount = ByteString.copyFrom(valueData)
                    data = ByteString.copyFrom(callData.decodeHex())
                }.build()
            }.build()
        }.build()

        return sign(input = input, privateKey)
    }

    internal fun buildSignInput(
        assetId: AssetId,
        amount: BigInteger,
        tokenAmount: BigInteger,
        fee: GasFee,
        chainId: BigInteger,
        nonce: BigInteger,
        destinationAddress: String,
        memo: String?,
        privateKey: ByteArray,
    ): Ethereum.SigningInput {
        return Ethereum.SigningInput.newBuilder().apply {
            when (chain.eip1559Support()) {
                true -> {
                    this.txMode = Ethereum.TransactionMode.Enveloped
                    this.maxFeePerGas = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
                    this.maxInclusionFeePerGas = ByteString.copyFrom(fee.minerFee.toByteArray())
                }

                false -> {
                    this.txMode = Ethereum.TransactionMode.Legacy
                    this.gasPrice = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
                }
            }
            this.gasLimit = ByteString.copyFrom(fee.limit.toByteArray())
            this.chainId = ByteString.copyFrom(chainId.toByteArray())
            this.nonce = ByteString.copyFrom(nonce.toByteArray())
            this.toAddress = EVMChain.getDestinationAddress(assetId, destinationAddress)
            this.privateKey = ByteString.copyFrom(privateKey)
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                this.transfer = Ethereum.Transaction.Transfer.newBuilder().apply {
                    this.amount = ByteString.copyFrom(amount.toByteArray())
                    this.data = ByteString.copyFrom(
                        EVMChain.encodeTransactionData(assetId, memo, tokenAmount, destinationAddress)
                    )
                }.build()
            }.build()
        }.build()
    }

    internal fun sign(input: Ethereum.SigningInput, privateKey: ByteArray): List<ByteArray> {
        val output = AnySigner.sign(input, coinType, Ethereum.SigningOutput.parser())
            .encoded
            .toByteArray()
        return listOf(output)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}