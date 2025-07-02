package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain
import com.wallet.core.primitives.FeePriority
import com.wallet.core.primitives.NFTType
import wallet.core.java.AnySigner
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumMessageSigner
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ethereum
import wallet.core.jni.proto.Ethereum.Transaction.ContractGeneric
import wallet.core.jni.proto.Ethereum.Transaction.ERC1155Transfer
import wallet.core.jni.proto.Ethereum.Transaction.ERC721Transfer
import wallet.core.jni.proto.Ethereum.Transaction.Transfer
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
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val transfer = buildTransfer(finalAmount, finalAmount, params)
        val input = buildSignInput(
            assetId = params.assetId,
            fee = meta.gasFee(feePriority),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination().address,
            transfer = transfer,
            privateKey = privateKey,
        )
        return sign(input)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger.ZERO
        val transfer = buildTransfer(amount, finalAmount, params)
        val input = buildSignInput(
            assetId = params.assetId,
            fee = meta.gasFee(feePriority),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination().address,
            transfer = transfer,
            privateKey = privateKey,
        )
        return sign(input)
    }

    override suspend fun signTokenApproval(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger.ZERO
        val transfer = buildTransfer(amount, finalAmount, params)
        val input = buildSignInput(
            assetId = params.assetId,
            fee = meta.gasFee(feePriority),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination()?.address ?: "",
            transfer = transfer,
            privateKey = privateKey,
        )
        return sign(input)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val approvalData = params.approval
        val chainData = chainData as EvmSignerPreloader.EvmChainData
        val amount = BigInteger(params.value)

        val approvalSign = if (approvalData != null) {
            signTokenApproval(
                params = ConfirmParams.Builder(params.asset, params.from)
                    .approval(
                        approvalData = encodeApprove(AnyAddress(approvalData.spender, CoinType.ETHEREUM).data()).toHexString(),
                        provider = "",
                        contract = approvalData.token,
                    ),
                chainData = chainData,
                finalAmount = BigInteger.ZERO,
                feePriority = feePriority,
                privateKey = privateKey
            )
        } else emptyList()

        val fee = chainData.gasFee(feePriority).let {
            if (approvalData == null) {
                it
            } else {
                GasFee(
                    feeAssetId = it.feeAssetId,
                    priority = it.priority,
                    limit = params.gasLimit!!,
                    maxGasPrice = it.maxGasPrice,
                    minerFee = it.minerFee,
                    amount = params.gasLimit!!.multiply(it.maxGasPrice)
                )
            }
        }
        val transfer = buildTransfer(amount, finalAmount, params)
        val swapInput = buildSignInput(
            assetId = AssetId(params.assetId.chain),
            fee = fee,
            chainId = chainData.chainId.toBigInteger(),
            nonce = chainData.nonce + if (approvalSign.isEmpty()) BigInteger.ZERO else BigInteger.ONE,
            destinationAddress = params.destination().address,
            transfer = transfer,
            privateKey = privateKey,
        )

        val swapSign = sign(swapInput)
        return approvalSign + swapSign
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, feePriority, privateKey)
    }

    override suspend fun signRedelegate(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, feePriority, privateKey)
    }

    override suspend fun signRewards(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, feePriority, privateKey)
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, feePriority, privateKey)
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return stakeSmartchain(params, chainData, finalAmount, feePriority, privateKey)
    }

    override suspend fun signNft(
        params: ConfirmParams.NftParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val transfer = when (params.nftAsset.tokenType) {
            NFTType.ERC721 -> ERC721Transfer.newBuilder().apply {
                this.from = params.from.address
                this.to = params.destination.address
                this.tokenId = ByteString.copyFrom(BigInteger(params.nftAsset.tokenId).abs().toByteArray())
            }.build()
            NFTType.ERC1155 -> ERC1155Transfer.newBuilder().apply {
                this.from = params.from.address
                this.to = params.destination.address
                this.tokenId = ByteString.copyFrom(BigInteger(params.nftAsset.tokenId).abs().toByteArray())
                this.value = ByteString.copyFrom(BigInteger.ONE.toByteArray())
            }.build()
            else -> throw IllegalArgumentException("Not supported token type")
        }

        val input = buildSignInput(
            assetId = AssetId(chain, params.nftAsset.contractAddress),
            fee = meta.gasFee(feePriority),
            chainId = meta.chainId.toBigInteger(),
            nonce = meta.nonce,
            destinationAddress = params.destination.address,
            privateKey = privateKey,
            transfer = transfer,
        )
        return sign(input)
    }

    private fun stakeSmartchain(
        params: ConfirmParams.Stake,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        if (params.assetId.chain != Chain.SmartChain) {
            throw Exception("Doesn't support")
        }
        val meta = chainData as EvmSignerPreloader.EvmChainData
        val fee = meta.gasFee(feePriority)
        val valueData = when (params) {
            is ConfirmParams.Stake.DelegateParams -> finalAmount.toByteArray()
            else -> BigInteger.ZERO.toByteArray()
        }
        val callData = StakeHub.encodeStake(params)
        val input = Ethereum.SigningInput.newBuilder().apply {
            this.txMode = Ethereum.TransactionMode.Enveloped
            this.maxFeePerGas = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
            this.maxInclusionFeePerGas = ByteString.copyFrom(fee.minerFee.toByteArray())
            this.gasLimit = ByteString.copyFrom(fee.limit.toByteArray())
            this.chainId = ByteString.copyFrom(meta.chainId.toBigInteger().toByteArray())
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

        return sign(input)
    }

    internal fun buildSignInput(
        assetId: AssetId,
        fee: GasFee,
        chainId: BigInteger,
        nonce: BigInteger,
        destinationAddress: String,
        transfer: Any,
        privateKey: ByteArray,
    ): Ethereum.SigningInput {
        return Ethereum.SigningInput.newBuilder().apply {
            this.txMode = Ethereum.TransactionMode.Enveloped
            this.maxFeePerGas = ByteString.copyFrom(fee.maxGasPrice.toByteArray())
            this.maxInclusionFeePerGas = ByteString.copyFrom(fee.minerFee.toByteArray())
            this.gasLimit = ByteString.copyFrom(fee.limit.toByteArray())
            this.chainId = ByteString.copyFrom(chainId.toByteArray())
            this.nonce = ByteString.copyFrom(nonce.toByteArray())
            this.toAddress = EVMChain.getDestinationAddress(assetId, destinationAddress)
            this.privateKey = ByteString.copyFrom(privateKey)
            this.transaction = Ethereum.Transaction.newBuilder().apply {
                when (transfer) {
                    is Transfer -> this.transfer = transfer
                    is ERC721Transfer -> this.erc721Transfer = transfer
                    is ERC1155Transfer -> this.erc1155Transfer = transfer
                    is ContractGeneric -> this.contractGeneric = transfer
                }

            }.build()
        }.build()
    }

    internal fun sign(input: Ethereum.SigningInput): List<ByteArray> {
        val output = AnySigner.sign(input, coinType, Ethereum.SigningOutput.parser())
            .encoded
            .toByteArray()
        return listOf(output)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    companion object {
        fun buildTransfer(amount: BigInteger, finalAmount: BigInteger, params: ConfirmParams): Transfer {
            return Transfer.newBuilder().apply {
                this.amount = ByteString.copyFrom(amount.toByteArray())
                this.data = ByteString.copyFrom(
                    when (params) {
                        is ConfirmParams.SwapParams -> EVMChain.encodeTransactionData(
                            params.assetId,
                            params.swapData,
                            finalAmount,
                            params.destination().address
                        )
                        else -> EVMChain.encodeTransactionData(params.assetId, params.memo(), finalAmount, params.destination()?.address ?: "")
                    }

                )
            }.build()
        }
    }
}