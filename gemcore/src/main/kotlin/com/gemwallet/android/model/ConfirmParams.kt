package com.gemwallet.android.model

import android.util.Size
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.BigIntegerSerializer
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarketPrice
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.PerpetualDirection
import com.wallet.core.primitives.Price
import com.wallet.core.primitives.Resource
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.Serializable
import org.json.JSONObject
import uniffi.gemstone.GemSwapQuoteDataType
import uniffi.gemstone.SwapperProvider
import java.math.BigInteger
import java.util.Base64

@Serializable
sealed class ConfirmParams() {

    abstract val asset: Asset

    abstract val from: Account

    @Serializable(BigIntegerSerializer::class)
    abstract val amount: BigInteger

    abstract val useMaxAmount: Boolean

    val assetId: AssetId get() = asset.id

    class Builder(
        val asset: Asset,
        val from: Account,
        val amount: BigInteger = BigInteger.ZERO,
        val useMaxAmount: Boolean = false,
    ) {
        fun transfer(destination: DestinationAddress, memo: String? = null): TransferParams {
            return when (asset.id.type()) {
                AssetSubtype.NATIVE -> TransferParams.Native(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    useMaxAmount = useMaxAmount
                )
                AssetSubtype.TOKEN -> TransferParams.Token(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    useMaxAmount = useMaxAmount
                )
            }
        }

        fun approval(approvalData: String, provider: String, contract: String = ""): TokenApprovalParams {
            return TokenApprovalParams(asset, from, approvalData, provider, contract)
        }

        fun delegate(validator: DelegationValidator) = Stake.DelegateParams(asset, from, amount, validator, useMaxAmount)

        fun rewards(validators: List<DelegationValidator>) = Stake.RewardsParams(asset, from, validators, amount)

        fun withdraw(delegation: Delegation) = Stake.WithdrawParams(
            asset = asset,
            from = from,
            amount = amount,
            delegation = delegation,
        )

        fun undelegate(delegation: Delegation): Stake.UndelegateParams {
            return Stake.UndelegateParams(
                asset,
                from,
                amount,
                delegation,
            )
        }

        fun redelegate(dstValidator: DelegationValidator, delegation: Delegation): Stake.RedelegateParams {
            return Stake.RedelegateParams(
                asset,
                from = from,
                amount,
                delegation,
                dstValidator,
                delegation.base.shares,
                delegation.base.balance,
            )
        }

        fun activate(): Activate {
            return Activate(asset, from)
        }

        fun freeze(resource: Resource): Stake.Freeze {
            return Stake.Freeze(asset, from, amount, resource, useMaxAmount)
        }

        fun unfreeze(resource: Resource): Stake.Unfreeze {
            return Stake.Unfreeze(asset, from, amount, resource)
        }

        fun perpetual(): PerpetualParams.Open {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    sealed class TransferParams : ConfirmParams() {
        abstract val destination: DestinationAddress
        abstract val memo: String?
        abstract val inputType: InputType?

        override fun destination(): DestinationAddress {
            return destination
        }

        override fun memo(): String? {
            return memo
        }

        @Serializable
        class Generic(
            val requestId: String,
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO,
            override val destination: DestinationAddress = DestinationAddress(""),
            override val memo: String? = null,
            override val useMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
            val name: String,
            val description: String,
            val url: String,
            val icon: String,
            val gasLimit: String?,
        ) : TransferParams() {
            override fun hashCode(): Int {
                var result = asset.hashCode()
                result = 31 * result + requestId.hashCode()
                result = 31 * result + from.hashCode()
                result = 31 * result + amount.hashCode()
                result = 31 * result + destination.hashCode()
                result = 31 * result + memo.hashCode()
                result = 31 * result + useMaxAmount.hashCode()
                result = 31 * result + name.hashCode()
                result = 31 * result + destination.hashCode()
                result = 31 * result + url.hashCode()
                result = 31 * result + icon.hashCode()
                result = 31 * result + (gasLimit?.hashCode() ?: 0)
                return result
            }
        }

        @Serializable
        class Native(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val inputType: InputType? = null,
            override val useMaxAmount: Boolean = false,
        ) : TransferParams()

        @Serializable
        class Token(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val useMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        enum class InputType {
            Signature,
            EncodeTransaction,
        }
    }

    @Serializable
    class TokenApprovalParams(
        override val asset: Asset,
        override val from: Account,
        val data: String,
        val provider: String,
        val contract: String,
    ) : ConfirmParams() {
        override val useMaxAmount: Boolean = false

        override val amount: BigInteger
            get() = BigInteger.ZERO

        override fun memo(): String? = data

        override fun destination(): DestinationAddress? {
            return DestinationAddress(contract)
        }
    }

    @Serializable
    class SwapParams(
        override val from: Account,
        val fromAsset: Asset,
        @Serializable(BigIntegerSerializer::class) val fromAmount: BigInteger,
        val toAsset: Asset,
        @Serializable(BigIntegerSerializer::class) val toAmount: BigInteger,
        val swapData: String,
        val memo: String?,
        val providerId: SwapperProvider,
        val providerName: String,
        val protocol: String,
        val protocolId: String,
        val toAddress: String,
        val value: String,
        val approval: ApprovalData? = null,
        val slippageBps: UInt,
        val etaInSeconds: UInt?,
        val dataType: GemSwapQuoteDataType,
        @Serializable(BigIntegerSerializer::class) val gasLimit: BigInteger? = null,
        override val useMaxAmount: Boolean = false,
    ) : ConfirmParams() {

        override val asset: Asset
            get() = fromAsset

        override val amount: BigInteger
            get() = fromAmount

        override fun destination(): DestinationAddress = DestinationAddress(toAddress)

        override fun memo(): String? = memo

        @Serializable
        data class ApprovalData(
            val token: String,
            var spender: String,
            var value: String
        )
    }

    @Serializable
    class Activate(
        override val asset: Asset,
        override val from: Account,
        @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO,
    ) : ConfirmParams() {
        override val useMaxAmount: Boolean
            get() = false

        override fun destination(): DestinationAddress? {
            return DestinationAddress(from.address)
        }
    }

    @Serializable
    class NftParams(
        override val asset: Asset,
        override val from: Account,
        val destination: DestinationAddress,
        val nftAsset: NFTAsset,
    ) : ConfirmParams() {
        override val useMaxAmount: Boolean
            get() = false

        @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO

        override fun destination(): DestinationAddress {
            return destination
        }
    }

    @Serializable
    sealed class Stake : ConfirmParams() {

        @Serializable
        class DelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val validator: DelegationValidator,
            override val useMaxAmount: Boolean = false,
        ) : Stake() {
            override fun destination(): DestinationAddress? {
                return DestinationAddress(validator.id)
            }
        }

        @Serializable
        class WithdrawParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val delegation: Delegation,
        ) : Stake() {
            override val useMaxAmount: Boolean
                get() = false

            override fun destination(): DestinationAddress? {
                return DestinationAddress(delegation.validator.id)
            }
        }

        @Serializable
        class UndelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val delegation: Delegation,
        ) : Stake() {
            override val useMaxAmount: Boolean
                get() = false

            override fun destination(): DestinationAddress? {
                return DestinationAddress(delegation.validator.id)
            }
        }

        @Serializable
        class RedelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val delegation: Delegation,
            val dstValidator: DelegationValidator,
            val share: String?,
            val balance: String?,
        ) : Stake() {
            override val useMaxAmount: Boolean
                get() = false

            override fun destination(): DestinationAddress? {
                return DestinationAddress("")
            }
        }

        @Serializable
        class RewardsParams(
            override val asset: Asset,
            override val from: Account,
            val validators: List<DelegationValidator>,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
        ) : Stake() {
            override val useMaxAmount: Boolean
                get() = false

            override fun destination(): DestinationAddress? {
                return DestinationAddress("")
            }
        }

        @Serializable
        class Freeze(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val resource: Resource,
            override val useMaxAmount: Boolean = false,
        ) : Stake() {
            override fun destination(): DestinationAddress? {
                return DestinationAddress("")
            }
        }

        @Serializable
        class Unfreeze(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val resource: Resource,
        ) : Stake() {
            override val useMaxAmount: Boolean
                get() = false

            override fun destination(): DestinationAddress? {
                return DestinationAddress("")
            }
        }
    }

    @Serializable
    sealed class PerpetualParams : ConfirmParams() {

        @Serializable
        class Open(
            val perpetualId: String,
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val useMaxAmount: Boolean = false,
            val direction: PerpetualDirection,
            val baseAsset: Asset,
            val assetIndex: Int,
            val price: String,
            val fiatValue: Double,
            val size: String,
            val slippage: Double,
            val leverage: Int,
            val entryPrice: Double?,
            val marketPrice: Double,
            val marginAmount: Double,
            val takeProfit: String?,
            val stopLoss: String?
        ) : PerpetualParams()


//        val id: String,
//	val name: String,
//	val provider: PerpetualProvider,
//	val assetId: AssetId,
//	val identifier: String,

//	val price: Double,
//	val pricePercentChange24h: Double,
//	val openInterest: Double,
//	val volume24h: Double,
//	val funding: Double,
//	val maxLeverage: UByte
    }

    fun pack(): String? {
        val json = jsonEncoder.encodeToString(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    fun getTxType() : TransactionType {
        return when (this) {
            is TransferParams -> TransactionType.Transfer
            is TokenApprovalParams -> TransactionType.TokenApproval
            is SwapParams -> TransactionType.Swap
            is Activate -> TransactionType.AssetActivation
            is NftParams -> TransactionType.TransferNFT
            is Stake.DelegateParams -> TransactionType.StakeDelegate
            is Stake.RewardsParams -> TransactionType.StakeRewards
            is Stake.RedelegateParams -> TransactionType.StakeRedelegate
            is Stake.UndelegateParams -> TransactionType.StakeUndelegate
            is Stake.WithdrawParams -> TransactionType.StakeWithdraw
            is Stake.Freeze -> TransactionType.StakeFreeze
            is Stake.Unfreeze -> TransactionType.StakeUnfreeze
            is Stake -> throw IllegalArgumentException("Invalid stake parameter")
            is PerpetualParams.Open -> TransactionType.PerpetualOpenPosition
        }
    }

    open  fun destination(): DestinationAddress? = null

    open fun memo(): String? = null

    override fun hashCode(): Int {
        return asset.id.toIdentifier().hashCode() +
                destination().hashCode() +
                memo().hashCode() +
                amount.hashCode() +
                useMaxAmount.hashCode()
    }

    companion object {
        fun unpack(input: String): ConfirmParams {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            val type = JSONObject(json).getString("type")
            val result = when (type) {
                TransferParams.Generic::class.qualifiedName -> jsonEncoder.decodeFromString<TransferParams.Generic>(json)
                TransferParams.Native::class.qualifiedName -> jsonEncoder.decodeFromString<TransferParams.Native>(json)
                TransferParams.Token::class.qualifiedName -> jsonEncoder.decodeFromString<TransferParams.Token>(json)
                SwapParams::class.qualifiedName -> jsonEncoder.decodeFromString<SwapParams>(json)
                TokenApprovalParams::class.qualifiedName -> jsonEncoder.decodeFromString<TokenApprovalParams>(json)
                Stake.DelegateParams::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.DelegateParams>(json)
                Stake.UndelegateParams::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.UndelegateParams>(json)
                Stake.RewardsParams::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.RewardsParams>(json)
                Stake.RedelegateParams::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.RedelegateParams>(json)
                Stake.WithdrawParams::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.WithdrawParams>(json)
                Stake.Freeze::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.Freeze>(json)
                Stake.Unfreeze::class.qualifiedName -> jsonEncoder.decodeFromString<Stake.Unfreeze>(json)
                Activate::class.qualifiedName -> jsonEncoder.decodeFromString<Activate>(json)
                NftParams::class.qualifiedName -> jsonEncoder.decodeFromString<NftParams>(json)
//                SmartContractCallParams::class.qualifiedName -> TODO()
                else -> throw IllegalStateException()
            }
            return result
        }
    }
}

fun uniffi.gemstone.GemApprovalData.toModel(): ConfirmParams.SwapParams.ApprovalData {
    return ConfirmParams.SwapParams.ApprovalData(
        token = this.token,
        spender = this.spender,
        value = this.value,
    )
}