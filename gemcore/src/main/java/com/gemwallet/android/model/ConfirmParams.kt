package com.gemwallet.android.model

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigInteger
import java.util.Base64

@Serializable
sealed class ConfirmParams {

    abstract val asset: Asset

    abstract val from: Account

    @Serializable(BigIntegerSerializer::class)
    abstract val amount: BigInteger

    val assetId: AssetId get() = asset.id

    class Builder(
        val asset: Asset,
        val from: Account,
        val amount: BigInteger = BigInteger.ZERO,
    ) {
        fun transfer(destination: DestinationAddress, memo: String? = null, isMax: Boolean = false): TransferParams {
            return when (asset.id.type()) {
                AssetSubtype.NATIVE -> TransferParams.Native(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
                AssetSubtype.TOKEN -> TransferParams.Token(
                    asset = asset,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
            }
        }

        fun approval(approvalData: String, provider: String, contract: String = ""): TokenApprovalParams {
            return TokenApprovalParams(asset, from, approvalData, provider, contract)
        }

        fun delegate(validatorId: String) = Stake.DelegateParams(asset, from, amount, validatorId)

        fun rewards(validatorsId: List<String>) = Stake.RewardsParams(asset, from, validatorsId, amount)

        fun withdraw(delegation: Delegation) = Stake.WithdrawParams(
            asset = asset,
            from = from,
            amount = amount,
            validatorId = delegation.validator.id,
            delegationId = delegation.base.delegationId,
        )

        fun undelegate(delegation: Delegation): Stake.UndelegateParams {
            return Stake.UndelegateParams(
                asset,
                from,
                amount,
                delegation.validator.id,
                delegation.base.delegationId,
                delegation.base.shares,
                delegation.base.balance
            )
        }

        fun redelegate(dstValidatorId: String, delegation: Delegation): Stake.RedelegateParams {
            return Stake.RedelegateParams(
                asset,
                from = from,
                amount,
                delegation.validator.id,
                dstValidatorId,
                delegation.base.shares,
                delegation.base.balance,
            )
        }

        fun activate(): Activate {
            return Activate(asset, from)
        }
    }

    @Serializable
    sealed class TransferParams : ConfirmParams() {
        abstract val destination: DestinationAddress
        abstract val memo: String?
        abstract val isMaxAmount: Boolean

        override fun isMax(): Boolean {
            return isMaxAmount
        }

        override fun destination(): DestinationAddress {
            return destination
        }

        override fun memo(): String? {
            return memo
        }

        @Serializable
        class Native(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
        ) : TransferParams()

        @Serializable
        class Token(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
        ) : TransferParams()
    }

    @Serializable
    class TokenApprovalParams(
        override val asset: Asset,
        override val from: Account,
        val data: String,
        val provider: String,
        val contract: String
    ) : ConfirmParams() {
        override val amount: BigInteger
            get() = BigInteger.ZERO
    }

    @Serializable
    class SwapParams(
        override val from: Account,
        val fromAsset: Asset,
        @Serializable(BigIntegerSerializer::class) val fromAmount: BigInteger,
        val toAssetId: AssetId,
        @Serializable(BigIntegerSerializer::class) val toAmount: BigInteger,
        val swapData: String,
        val provider: String,
        val protocolId: String,
        val to: String,
        val value: String,
        val approval: ApprovalData? = null,
        @Serializable(BigIntegerSerializer::class) val gasLimit: BigInteger? = null,
    ) : ConfirmParams() {

        override val asset: Asset
            get() = fromAsset
        override val amount: BigInteger
            get() = fromAmount
        override fun destination(): DestinationAddress = DestinationAddress(to)

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
    ) : ConfirmParams()

    @Serializable
    sealed class Stake : ConfirmParams() {
        abstract val validatorId: String

        @Serializable
        class DelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val validatorId: String,
        ) : Stake()

        @Serializable
        class WithdrawParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val validatorId: String,
            val delegationId: String,
        ) : Stake()

        @Serializable
        class UndelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val validatorId: String,
            val delegationId: String,
            val share: String?,
            val balance: String?
        ) : Stake()

        @Serializable
        class RedelegateParams(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            val srcValidatorId: String,
            val dstValidatorId: String,
            val share: String?,
            val balance: String?,
        ) : Stake() {
            override val validatorId: String = ""
        }

        @Serializable
        class RewardsParams(
            override val asset: Asset,
            override val from: Account,
            val validatorsId: List<String>,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
        ) : Stake() {
            override val validatorId: String = ""
        }
    }

    fun pack(): String? {
        val json = jsonEncoder.encodeToString(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    fun getTxType() : TransactionType {
        return when (this) {
            is TransferParams  -> TransactionType.Transfer
            is TokenApprovalParams  -> TransactionType.TokenApproval
            is SwapParams  -> TransactionType.Swap
            is Activate  -> TransactionType.AssetActivation
            is Stake.DelegateParams  -> TransactionType.StakeDelegate
            is Stake.RewardsParams -> TransactionType.StakeRewards
            is Stake.RedelegateParams  -> TransactionType.StakeRedelegate
            is Stake.UndelegateParams  -> TransactionType.StakeUndelegate
            is Stake.WithdrawParams  -> TransactionType.StakeWithdraw
            is Stake -> throw IllegalArgumentException("Invalid stake parameter")
        }
    }

    open fun destination(): DestinationAddress? = null

    open fun memo(): String? = null

    open fun isMax(): Boolean = false

    override fun hashCode(): Int {
        return asset.id.toIdentifier().hashCode() +
                destination().hashCode() +
                memo().hashCode() +
                amount.hashCode() +
                isMax().hashCode()
    }

    companion object {
        fun unpack(txType: TransactionType, input: String): ConfirmParams {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            val result = when (txType) {
                TransactionType.Transfer -> jsonEncoder.decodeFromString<TransferParams.Native>(json)
                TransactionType.Swap -> jsonEncoder.decodeFromString<SwapParams>(json)
                TransactionType.TokenApproval -> jsonEncoder.decodeFromString<TokenApprovalParams>(json)
                TransactionType.StakeDelegate -> jsonEncoder.decodeFromString<Stake.DelegateParams>(json)
                TransactionType.StakeUndelegate -> jsonEncoder.decodeFromString<Stake.UndelegateParams>(json)
                TransactionType.StakeRewards -> jsonEncoder.decodeFromString<Stake.RewardsParams>(json)
                TransactionType.StakeRedelegate -> jsonEncoder.decodeFromString<Stake.RedelegateParams>(json)
                TransactionType.StakeWithdraw -> jsonEncoder.decodeFromString<Stake.WithdrawParams>(json)
                TransactionType.AssetActivation -> jsonEncoder.decodeFromString<Activate>(json)
                TransactionType.TransferNFT -> TODO()
                TransactionType.SmartContractCall -> TODO()
            }

            return if (result.asset.id.type() == AssetSubtype.TOKEN && result is TransferParams.Native) {
                TransferParams.Token(
                    result.asset,
                    result.from,
                    result.amount,
                    result.destination,
                    result.memo,
                    result.isMaxAmount,
                )
            } else {
                result
            }
        }
    }
}

fun uniffi.gemstone.ApprovalData.toModel(): ConfirmParams.SwapParams.ApprovalData {
    return ConfirmParams.SwapParams.ApprovalData(
        token = this.token,
        spender = this.spender,
        value = this.value,
    )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = BigInteger::class)
private object BigIntegerSerializer {
    override fun serialize(encoder: Encoder, value: BigInteger) = when (encoder) {
        is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(value.toString()))
        else -> encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigInteger = when (decoder) {
        is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigInteger()
        else -> decoder.decodeString().toBigInteger()
    }
}