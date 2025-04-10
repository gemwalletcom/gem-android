package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom
import com.wallet.core.primitives.FeePriority
import wallet.core.java.AnySigner
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Cosmos
import wallet.core.jni.proto.Cosmos.Amount
import wallet.core.jni.proto.Cosmos.Message
import wallet.core.jni.proto.Cosmos.Message.Delegate
import wallet.core.jni.proto.Cosmos.Message.Undelegate
import wallet.core.jni.proto.Cosmos.Message.WithdrawDelegationReward
import java.math.BigInteger

class CosmosSignClient(
    private val chain: Chain,
) : SignClient {

    val coin = WCChainTypeProxy().invoke(chain)

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = CosmosDenom.from(chain)
        val message = getTransferMessage(
            from = params.from.address,
            recipient = params.destination().address,
            coin = coin,
            amount = getAmount(finalAmount, denom = denom)
        )
        return sign(chainData, message, params.memo() ?: "", privateKey)
    }


    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = params.asset.id.tokenId!!

        val message = getTransferMessage(
            from = params.from.address,
            recipient = params.destination().address,
            coin = coin,
            amount = getAmount(finalAmount, denom = denom)
        )
        return sign(chainData, message, params.memo() ?: "", privateKey)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = CosmosDenom.from(chain)
        val message = when (chain) {
            Chain.Thorchain -> listOf(getThorChainSwapMessage(params, coin))
            else -> getTransferMessage(
                from = params.from.address,
                recipient = params.destination().address,
                coin = coin,
                amount = getAmount(finalAmount, denom = denom)
            )
        }
        val memo = params.swapData ?: throw IllegalArgumentException("No swap data")
        return sign(chainData, message, memo, privateKey)
    }

    override suspend fun signTokenApproval(
        params: ConfirmParams.TokenApprovalParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        throw IllegalArgumentException()
    }

    override suspend fun signDelegate(
        params: ConfirmParams.Stake.DelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = CosmosDenom.from(chain)
        val message = getStakeMessage(params.from.address, params.validatorId, getAmount(finalAmount, denom))
        return sign(chainData, message, "Stake via Gem Wallet", privateKey)
    }

    override suspend fun signRedelegate(
        params: ConfirmParams.Stake.RedelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = CosmosDenom.from(chain)
        val message = getRedelegateMessage(
            delegatorAddress = params.from.address,
            validatorSrcAddress = params.srcValidatorId,
            validatorDstAddress = params.dstValidatorId,
            amount = getAmount(params.amount, denom),
        )
        return sign(chainData, message, "Stake via Gem Wallet", privateKey)
    }

    override suspend fun signRewards(
        params: ConfirmParams.Stake.RewardsParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val message = getRewardsMessage(params.from.address, params.validatorsId)
        return sign(chainData, message, "Stake via Gem Wallet", privateKey)
    }

    override suspend fun signUndelegate(
        params: ConfirmParams.Stake.UndelegateParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val denom = CosmosDenom.from(chain)
        val message = getUnstakeMessage(params.from.address, params.validatorId, getAmount(params.amount, denom))
        return sign(chainData, message, "Stake via Gem Wallet", privateKey)
    }

    override suspend fun signWithdraw(
        params: ConfirmParams.Stake.WithdrawParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        priority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        throw IllegalArgumentException()
    }

    private fun getThorChainSwapMessage(params: ConfirmParams.SwapParams, coinType: CoinType): Message {
        val message = Message.newBuilder().apply {
            this.setThorchainDepositMessage(
                Message.THORChainDeposit.newBuilder().apply {
                    addCoins(
                        Cosmos.THORChainCoin.newBuilder().apply {
                            this.amount = params.amount.toString()
                            this.asset = Cosmos.THORChainAsset.newBuilder().apply {
                                this.chain = "THOR"
                                this.symbol = "RUNE"
                                this.ticker = "RUNE"
                            }.build()
                        }
                    )
                    this.memo = params.swapData
                    this.signer = ByteString.copyFrom(AnyAddress(params.from.address, coinType).data())
                }
            )
        }.build()
        return message
    }

    private fun getTransferMessage(
        from: String,
        recipient: String,
        coin: CoinType,
        amount: Amount,
    ): List<Message> {
        val message = Message.newBuilder()
        when (chain) {
            Chain.Thorchain -> message.setThorchainSendMessage(
                Message.THORChainSend.newBuilder().apply {
                    fromAddress = ByteString.copyFrom(AnyAddress(from, coin).data())
                    toAddress = ByteString.copyFrom(AnyAddress(recipient, coin).data())
                    addAmounts(amount)
                }
            )
            Chain.Cosmos,
            Chain.Celestia,
            Chain.Injective,
            Chain.Sei,
            Chain.Noble,
            Chain.Osmosis -> message.setSendCoinsMessage(
                Message.Send.newBuilder().apply {
                    fromAddress = from
                    toAddress = recipient
                    addAmounts(amount)
                }
            )
            else -> throw IllegalArgumentException()
        }
        return listOf(message.build())
    }

    private fun getStakeMessage(delegatorAddress: String, validatorAddress: String, amount: Amount): List<Message> {
        val message = Message.newBuilder().apply {
            stakeMessage = Delegate.newBuilder().apply {
                this.amount = amount
                this.delegatorAddress = delegatorAddress
                this.validatorAddress = validatorAddress
            }.build()
        }.build()
        return listOf(message)
    }

    fun getRewardsMessage(delegatorAddress: String, validators: List<String>): List<Message> {
        return validators.map { validator ->
            Message.newBuilder().apply {
                withdrawStakeRewardMessage = WithdrawDelegationReward.newBuilder().apply {
                    this.delegatorAddress = delegatorAddress
                    this.validatorAddress = validator
                }.build()
            }.build()
        }
    }

    fun getUnstakeMessage(delegatorAddress: String, validatorAddress: String, amount: Amount): List<Message> {
        val message = Message.newBuilder().apply {
            unstakeMessage = Undelegate.newBuilder().apply {
                this.amount = amount
                this.delegatorAddress = delegatorAddress
                this.validatorAddress = validatorAddress
            }.build()
        }.build()
        return listOf(message)
    }

    fun getRedelegateMessage(
        delegatorAddress: String,
        validatorSrcAddress: String,
        validatorDstAddress: String,
        amount: Amount,
    ): List<Message> {
        val message = Message.newBuilder().apply {
            restakeMessage = Message.BeginRedelegate.newBuilder().apply {
                this.amount = amount
                this.delegatorAddress = delegatorAddress
                this.validatorSrcAddress = validatorSrcAddress
                this.validatorDstAddress = validatorDstAddress
            }.build()
        }.build()
        return listOf(message)
    }

    private fun getAmount(amount: BigInteger, denom: String): Amount {
        return Amount.newBuilder().apply {
            this.amount = amount.toString()
            this.denom = denom
        }.build()
    }

    private fun sign(
        chainData: ChainSignData,
        messages: List<Message>,
        memo: String,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as CosmosSignerPreloader.CosmosChainData
        val fee = meta.fee() as GasFee
        val feeAmount = fee.amount
        val gas = fee.limit.toLong() * messages.size
        val coin = WCChainTypeProxy().invoke(chain)

        val cosmosFee = Cosmos.Fee.newBuilder().apply {
            this.gas = gas
            when (chain) {
                Chain.Cosmos,
                Chain.Osmosis,
                Chain.Celestia,
                Chain.Injective,
                Chain.Sei,
                Chain.Noble -> addAmounts(getAmount(feeAmount, CosmosDenom.from(chain)))
                else -> {}
            }

        }.build()
        val signInput = Cosmos.SigningInput.newBuilder().apply {
            this.mode = Cosmos.BroadcastMode.SYNC
            this.signingMode = Cosmos.SigningMode.Protobuf
            this.chainId = meta.chainId
            this.accountNumber = meta.accountNumber
            this.sequence = meta.sequence
            this.fee = cosmosFee
            this.memo = memo
            this.privateKey = ByteString.copyFrom(privateKey)
            messages.forEach {
                this.addMessages(it)
            }
        }
        val sign = AnySigner.sign(signInput.build(), coin, Cosmos.SigningOutput.parser())
        return listOf(sign.serialized.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}