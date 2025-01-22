package com.gemwallet.android.blockchain.clients.tron

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Tron
import java.math.BigInteger

class TronSignClient(
    private val chain: Chain,
) : SignClient {

    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun signTransaction(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): List<ByteArray> {
        return when (params.input) {
            is ConfirmParams.Stake.DelegateParams -> signDelegate(params, privateKey)
            is ConfirmParams.Stake.RedelegateParams -> signRedelegate(params, privateKey)
            is ConfirmParams.Stake.RewardsParams -> signRewards(params, privateKey)
            is ConfirmParams.Stake.UndelegateParams -> signUndelegate(params, privateKey)
            is ConfirmParams.Stake.WithdrawParams -> signWithdraw(params, privateKey)
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.SwapParams -> throw IllegalArgumentException("Not supported")
            is ConfirmParams.TransferParams.Native,
            is ConfirmParams.TransferParams.Token -> signTransfer(params, txSpeed, privateKey)
        }
    }

    private fun signTransfer(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): List<ByteArray> {
        val blockInfo = params.chainData as TronSignerPreloader.TronChainData
        val transaction = Tron.Transaction.newBuilder().apply {
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = blockInfo.number
                this.parentHash = ByteString.copyFrom(blockInfo.parentHash.decodeHex())
                this.timestamp = blockInfo.timestamp
                this.version = blockInfo.version.toInt()
                this.witnessAddress = ByteString.copyFrom(blockInfo.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(blockInfo.txTrieRoot.decodeHex())
            }.build()
            when (params.input.assetId.type()) {
                AssetSubtype.NATIVE -> this.transfer = getTransferContract(params.finalAmount, params.input.from.address, params.input.destination()?.address ?: "")
                AssetSubtype.TOKEN -> this.transferTrc20Contract = getTransferTRC20Contract(
                    params.input.assetId.tokenId!!,
                    params.finalAmount,
                    params.input.from.address,
                    params.input.destination()?.address ?: ""
                )
                else -> throw IllegalArgumentException("Unsupported type")
            }
            this.expiration = blockInfo.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = blockInfo.timestamp
            this.feeLimit = blockInfo.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return listOf(signingOutput.json.toByteArray())
    }

    fun signDelegate(params: SignerParams, privateKey: ByteArray): List<ByteArray> {

        val freezContract = Tron.FreezeBalanceV2Contract.newBuilder().apply {
            this.ownerAddress = params.input.from.address
            this.frozenBalance = params.input.amount.toLong()
            this.resource = "BANDWIDTH"
        }.build()
        val voteContract = createVoteContract(params)
        return listOf(
            sign(params, freezContract, privateKey),
            sign(params, voteContract, privateKey),
        )
    }

    fun signUndelegate(params: SignerParams, privateKey: ByteArray): List<ByteArray> {
        val chainData = params.chainData as TronSignerPreloader.TronChainData
        val votes = chainData.votes
        return listOf(
            Tron.UnfreezeBalanceV2Contract.newBuilder().apply {
                this.ownerAddress = params.input.from.address
                this.unfreezeBalance = params.input.amount.toLong()
                this.resource = "BANDWIDTH"
            }.build(),
            if (votes.isEmpty()) null else createVoteContract(params),
        )
        .filterNotNull()
        .map {
            sign(params, it, privateKey)
        }
    }

    fun signRedelegate(params: SignerParams, privateKey: ByteArray): List<ByteArray> {
        val voteContract = createVoteContract(params)
        return listOf(sign(params, voteContract, privateKey))
    }

    fun signRewards(params: SignerParams, privateKey: ByteArray): List<ByteArray> {
        val contract = Tron.WithdrawBalanceContract.newBuilder().apply {
            this.ownerAddress = params.input.from.address
        }.build()
        return listOf(
            sign(params, contract, privateKey)
        )
    }

    fun signWithdraw(params: SignerParams, privateKey: ByteArray): List<ByteArray> {
        val contract = Tron.WithdrawExpireUnfreezeContract.newBuilder().apply {
            this.ownerAddress = params.input.from.address
        }.build()
        return listOf(
            sign(params, contract, privateKey)
        )
    }

    private fun getTransferContract(value: BigInteger, ownerAddress: String, recipient: String)
        = Tron.TransferContract.newBuilder().apply {
            this.amount = value.toLong()
            this.ownerAddress = ownerAddress
            this.toAddress = recipient
        }.build()

    private fun getTransferTRC20Contract(tokenId: String, value: BigInteger, ownerAddress: String, recipient: String)
            = Tron.TransferTRC20Contract.newBuilder().apply {
        this.contractAddress = tokenId
        this.ownerAddress = ownerAddress
        this.toAddress = recipient
        this.amount = ByteString.copyFrom(value.toByteArray())
    }.build()

    private fun createVoteContract(params: SignerParams) = Tron.VoteWitnessContract.newBuilder().apply {
            this.ownerAddress = params.input.from.address
            this.support = true
            this.addAllVotes(
                (params.chainData as TronSignerPreloader.TronChainData).votes.map {
                    Tron.VoteWitnessContract.Vote.newBuilder().apply {
                        this.voteAddress = it.key
                        this.voteCount = it.value
                    }.build()
                }
            )
        }.build()

    private fun sign(params: SignerParams, contract: Any, privateKey: ByteArray): ByteArray {
        val blockInfo = params.chainData as TronSignerPreloader.TronChainData
        val transaction = Tron.Transaction.newBuilder().apply {
            when (contract) {
                is Tron.TransferContract -> this.transfer = contract
                is Tron.TransferTRC20Contract -> this.transferTrc20Contract = contract
                is Tron.FreezeBalanceV2Contract -> this.freezeBalanceV2 = contract
                is Tron.VoteWitnessContract -> this.voteWitness = contract
                is Tron.UnfreezeBalanceV2Contract -> this.unfreezeBalanceV2 = contract
                is Tron.WithdrawExpireUnfreezeContract -> this.withdrawExpireUnfreeze = contract
                is Tron.WithdrawBalanceContract -> this.withdrawBalance = contract
            }
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = blockInfo.number
                this.parentHash = ByteString.copyFrom(blockInfo.parentHash.decodeHex())
                this.timestamp = blockInfo.timestamp
                this.version = blockInfo.version.toInt()
                this.witnessAddress = ByteString.copyFrom(blockInfo.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(blockInfo.txTrieRoot.decodeHex())
            }.build()

            this.expiration = blockInfo.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = blockInfo.timestamp
            this.feeLimit = blockInfo.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return signingOutput.json.toByteArray()
    }
}