package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.eip1559Support
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
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

    override suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): List<ByteArray> {
        when (params.input) {
            is ConfirmParams.Stake.RedelegateParams,
            is ConfirmParams.Stake.DelegateParams,
            is ConfirmParams.Stake.UndelegateParams,
            is ConfirmParams.Stake.RewardsParams,
            is ConfirmParams.Stake.WithdrawParams -> when (params.input.assetId.chain) {
                Chain.SmartChain -> {
                    return stakeSmartchain(params, txSpeed, privateKey)
                }
                else -> throw IllegalArgumentException()
            }
            else -> {}
        }
        val meta = params.chainData as EvmSignerPreloader.EvmChainData
        val coinType = WCChainTypeProxy().invoke(chain)
        val input = params.input
        val amount = when (input) {
            is ConfirmParams.SwapParams -> BigInteger(input.value)
            is ConfirmParams.TokenApprovalParams -> BigInteger.ZERO
            is ConfirmParams.TransferParams -> when (params.input.assetId.type()) {
                AssetSubtype.NATIVE -> params.finalAmount
                AssetSubtype.TOKEN -> BigInteger.ZERO
            }
            else -> throw IllegalArgumentException()
        }
        val signInput = buildSignInput(
            assetId = when (input) {
                is ConfirmParams.SwapParams -> AssetId(input.assetId.chain)
                else -> params.input.assetId
            },
            amount = amount,
            tokenAmount = params.finalAmount,
            fee = meta.gasFee(txSpeed),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.input.destination()?.address ?: "",
            memo = when (input) {
                is ConfirmParams.SwapParams -> input.swapData
                is ConfirmParams.TokenApprovalParams -> input.data
                else -> input.memo()
            },
            privateKey = privateKey,
        )
        val output = AnySigner.sign(signInput, coinType, Ethereum.SigningOutput.parser())
            .encoded
            .toByteArray()
        return listOf(output)
    }

    private fun stakeSmartchain(params: SignerParams, speed: TxSpeed, privateKey: ByteArray): List<ByteArray> {
        val meta = params.chainData as EvmSignerPreloader.EvmChainData
        val fee = meta.gasFee(speed) ?: throw IllegalArgumentException()
        val valueData = when (params.input) {
            is ConfirmParams.Stake.DelegateParams -> params.finalAmount.toByteArray()
            else -> BigInteger.ZERO.toByteArray()
        }
        val callData = StakeHub.encodeStake(params.input)
        val signInput = Ethereum.SigningInput.newBuilder().apply {
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
        val coinType = WCChainTypeProxy().invoke(chain)
        val output = AnySigner.sign(signInput, coinType, Ethereum.SigningOutput.parser())
            .encoded
            .toByteArray()
        return listOf(output)
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
    ): Ethereum.SigningInput? {
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

    override fun supported(chain: Chain): Boolean = this.chain == chain
}