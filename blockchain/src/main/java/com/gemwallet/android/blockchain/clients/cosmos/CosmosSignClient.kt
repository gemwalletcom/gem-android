package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom
import com.wallet.core.primitives.TransactionType
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

    override suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray,
    ): ByteArray {
        val from = params.input.from.address
        val coin = WCChainTypeProxy().invoke(chain)
        val input = params.input
        val denom = if (input.assetId.type() == AssetSubtype.NATIVE) CosmosDenom.from(chain) else input.assetId.tokenId!!
        val message = when (input) {
            is ConfirmParams.TransferParams -> getTransferMessage(
                from = from,
                recipient = input.destination().address,
                coin = coin,
                amount = getAmount(params.finalAmount, denom = denom)
            )
            is ConfirmParams.Stake.DelegateParams -> getStakeMessage(from, input.validatorId, getAmount(params.finalAmount, denom))
            is ConfirmParams.Stake.RedelegateParams -> getRedelegateMessage(
                delegatorAddress = from,
                validatorSrcAddress = input.srcValidatorId,
                validatorDstAddress = input.dstValidatorId,
                amount = getAmount(params.input.amount, denom),
            )
            is ConfirmParams.Stake.RewardsParams -> getRewardsMessage(from, input.validatorsId)
            is ConfirmParams.Stake.UndelegateParams -> getUnstakeMessage(from, input.validatorId, getAmount(params.input.amount, denom))
            is ConfirmParams.SwapParams -> when (chain) {
                Chain.Thorchain -> listOf(getThorChainSwapMessage(params, coin))
                else -> getTransferMessage(
                    from = from,
                    recipient = input.destination().address,
                    coin = coin,
                    amount = getAmount(params.finalAmount, denom = denom)
                )
            }
            is ConfirmParams.TokenApprovalParams,
            is ConfirmParams.Stake.WithdrawParams -> throw IllegalArgumentException()
        }
        return sign(params, privateKey, message)
    }

    private fun getThorChainSwapMessage(params: SignerParams, coinType: CoinType): Message {
        val swapParams = params.input as ConfirmParams.SwapParams
        val message = Message.newBuilder().apply {
            this.setThorchainDepositMessage(
                Message.THORChainDeposit.newBuilder().apply {
                    addCoins(
                        Cosmos.THORChainCoin.newBuilder().apply {
                            this.amount = params.input.amount.toString()
                            this.asset = Cosmos.THORChainAsset.newBuilder().apply {
                                this.chain = "THOR"
                                this.symbol = "RUNE"
                                this.ticker = "RUNE"
                            }.build()
                        }
                    )
                    this.memo = swapParams.swapData
                    this.signer = ByteString.copyFrom(AnyAddress(params.input.from.address, coinType).data())
                }
            )
        }.build()
        return message
    }

//    return CosmosMessage.with {
//                $0.thorchainDepositMessage = CosmosMessage.THORChainDeposit.with {
//                    $0.coins = [
//                        CosmosTHORChainCoin.with {
//                            $0.amount = input.value.description
//                            $0.asset = CosmosTHORChainAsset.with {
//                                $0.chain = chainName
//                                $0.symbol = symbol
//                                $0.ticker = symbol
//                            }
//                        }
//                    ]
//                    $0.memo = memo
//                    $0.signer = AnyAddress(string: input.senderAddress, coin: chain.chain.coinType)!.data
//                }
//            }

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

    private fun sign(input: SignerParams, privateKey: ByteArray, messages: List<Message>): ByteArray {
        val meta = input.chainData as CosmosSignerPreloader.CosmosChainData
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
        val memo = when (input.input.getTxType()) {
            TransactionType.StakeDelegate,
            TransactionType.StakeUndelegate,
            TransactionType.StakeRewards,
            TransactionType.StakeRedelegate,
            TransactionType.StakeWithdraw -> "Stake via Gem Wallet"
            TransactionType.Swap -> (input.input as? ConfirmParams.SwapParams)?.swapData ?: throw IllegalArgumentException("No swap data") // TODO: Doesn't for Throchain
            TransactionType.Transfer,
            TransactionType.TokenApproval -> input.input.memo() ?: ""
            TransactionType.AssetActivation -> throw IllegalArgumentException("asset activation doesn't support")
        }

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
        return sign.serialized.toByteArray()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}