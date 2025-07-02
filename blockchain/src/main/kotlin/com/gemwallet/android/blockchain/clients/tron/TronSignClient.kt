package com.gemwallet.android.blockchain.clients.tron

import android.text.format.DateUtils
import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.clients.ethereum.encodeApprove
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import wallet.core.java.AnySigner
import wallet.core.jni.AnyAddress
import wallet.core.jni.Base58
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.proto.Tron
import wallet.core.jni.proto.Tron.TransferContract
import wallet.core.jni.proto.Tron.TransferTRC20Contract
import java.math.BigInteger

class TronSignClient(
    private val chain: Chain,
) : SignClient {

    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val contract = TransferContract.newBuilder().apply {
            this.amount = finalAmount.toLong()
            this.ownerAddress = params.from.address
            this.toAddress = params.destination().address
        }.build()
        return signTransfer(chainData, contract, privateKey)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val contract = TransferTRC20Contract.newBuilder().apply {
            this.contractAddress = params.assetId.tokenId!!
            this.ownerAddress = params.from.address
            this.toAddress = params.destination().address
            this.amount = ByteString.copyFrom(finalAmount.toByteArray())
        }.build()
        return signTransfer(chainData, contract, privateKey)
    }

    private fun signTransfer(
        chainData: TronSignerPreloader.TronChainData,
        contract: Any,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val transaction = Tron.Transaction.newBuilder().apply {
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = chainData.number
                this.parentHash = ByteString.copyFrom(chainData.parentHash.decodeHex())
                this.timestamp = chainData.timestamp
                this.version = chainData.version.toInt()
                this.witnessAddress = ByteString.copyFrom(chainData.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(chainData.txTrieRoot.decodeHex())
            }.build()
            when (contract) {
                is TransferTRC20Contract -> this.transferTrc20Contract = contract
                is TransferContract -> this.transfer = contract
            }
            this.expiration = chainData.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = chainData.timestamp
            this.feeLimit = chainData.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return listOf(signingOutput.json.toByteArray())
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val freezContract = Tron.FreezeBalanceV2Contract.newBuilder().apply {
            this.ownerAddress = params.from.address
            this.frozenBalance = params.amount.toLong()
            this.resource = "BANDWIDTH"
        }.build()
        val voteContract = createVoteContract(chainData, params.from.address)
        return listOf(
            sign(chainData, freezContract, privateKey),
            sign(chainData, voteContract, privateKey),
        )
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val votes = chainData.votes
        return listOfNotNull(
            Tron.UnfreezeBalanceV2Contract.newBuilder().apply {
                this.ownerAddress = params.from.address
                this.unfreezeBalance = params.amount.toLong()
                this.resource = "BANDWIDTH"
            }.build(),
            if (votes.isEmpty()) null else createVoteContract(chainData, params.from.address),
        )
        .map {
            sign(chainData, it, privateKey)
        }
    }

    override suspend fun signRedelegate(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val voteContract = createVoteContract(chainData, params.from.address)
        return listOf(sign(chainData, voteContract, privateKey))
    }

    override suspend fun signRewards(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val contract = Tron.WithdrawBalanceContract.newBuilder().apply {
            this.ownerAddress = params.from.address
        }.build()
        return listOf(
            sign(chainData, contract, privateKey)
        )
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val contract = Tron.WithdrawExpireUnfreezeContract.newBuilder().apply {
            this.ownerAddress = params.from.address
        }.build()
        return listOf(
            sign(chainData, contract, privateKey)
        )
    }

    override suspend fun signTokenApproval(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val spender = Base58.decodeNoCheck(params.data).drop(0).toByteArray()
        val callData = encodeApprove(spender)
        val approvalContract = Tron.TriggerSmartContract.newBuilder().apply {
            ownerAddress = params.from.address
            contractAddress = params.contract
            data = ByteString.copyFrom(callData)
        }
        return listOf(sign(chainData as TronSignerPreloader.TronChainData, approvalContract, privateKey))
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = chainData as TronSignerPreloader.TronChainData
        val data = params.swapData
        val callValue = params.value.toLong()
        val contract = Tron.TriggerSmartContract.newBuilder().apply {
            this.ownerAddress = params.from.address
            this.contractAddress = params.to
            this.data = ByteString.copyFrom(data.toByteArray())
            this.callValue = callValue
        }
        val approvalData = params.approval
        val approval = approvalData?.let {
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
        } ?: emptyList()
        return listOf(sign(chainData, contract, privateKey)) + approval
    }

    private fun createVoteContract(chainData: TronSignerPreloader.TronChainData, owner: String) = Tron.VoteWitnessContract.newBuilder().apply {
            this.ownerAddress = owner
            this.support = true
            this.addAllVotes(
                chainData.votes.map {
                    Tron.VoteWitnessContract.Vote.newBuilder().apply {
                        this.voteAddress = it.key
                        this.voteCount = it.value
                    }.build()
                }
            )
        }.build()

    private fun sign(chainData: TronSignerPreloader.TronChainData, contract: Any, privateKey: ByteArray): ByteArray {
        val transaction = Tron.Transaction.newBuilder().apply {
            when (contract) {
                is TransferContract -> this.transfer = contract
                is TransferTRC20Contract -> this.transferTrc20Contract = contract
                is Tron.FreezeBalanceV2Contract -> this.freezeBalanceV2 = contract
                is Tron.VoteWitnessContract -> this.voteWitness = contract
                is Tron.UnfreezeBalanceV2Contract -> this.unfreezeBalanceV2 = contract
                is Tron.WithdrawExpireUnfreezeContract -> this.withdrawExpireUnfreeze = contract
                is Tron.WithdrawBalanceContract -> this.withdrawBalance = contract
                is Tron.TriggerSmartContract -> this.triggerSmartContract = contract
            }
            this.blockHeader = Tron.BlockHeader.newBuilder().apply {
                this.number = chainData.number
                this.parentHash = ByteString.copyFrom(chainData.parentHash.decodeHex())
                this.timestamp = chainData.timestamp
                this.version = chainData.version.toInt()
                this.witnessAddress = ByteString.copyFrom(chainData.witnessAddress.decodeHex())
                this.txTrieRoot = ByteString.copyFrom(chainData.txTrieRoot.decodeHex())
            }.build()

            this.expiration = chainData.timestamp + 10 * DateUtils.HOUR_IN_MILLIS
            this.timestamp = chainData.timestamp
            this.feeLimit = chainData.fee().amount.toLong()
        }
        val signInput = Tron.SigningInput.newBuilder().apply {
            this.transaction = transaction.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val signingOutput = AnySigner.sign(signInput, CoinType.TRON, Tron.SigningOutput.parser())
        return signingOutput.json.toByteArray()
    }
}