package com.gemwallet.android.model

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.BigIntegerSerializer
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.NFTAsset
import com.wallet.core.primitives.TransactionType
import kotlinx.serialization.Serializable
import org.json.JSONObject
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
        abstract val inputType: InputType?

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
        class Generic(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO,
            override val destination: DestinationAddress = DestinationAddress(""),
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        class Native(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
            override val inputType: InputType? = null,
        ) : TransferParams()

        @Serializable
        class Token(
            override val asset: Asset,
            override val from: Account,
            @Serializable(BigIntegerSerializer::class) override val amount: BigInteger,
            override val destination: DestinationAddress,
            override val memo: String? = null,
            override val isMaxAmount: Boolean = false,
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
    class NftParams(
        override val asset: Asset,
        override val from: Account,
        val destination: DestinationAddress,
        val nftAsset: NFTAsset,
    ) : ConfirmParams() {
        @Serializable(BigIntegerSerializer::class) override val amount: BigInteger = BigInteger.ZERO

        override fun destination(): DestinationAddress {
            return destination
        }
    }

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
                Activate::class.qualifiedName -> jsonEncoder.decodeFromString<Activate>(json)
                NftParams::class.qualifiedName -> jsonEncoder.decodeFromString<NftParams>(json)
//                SmartContractCallParams::class.qualifiedName -> TODO()
                else -> throw IllegalStateException()
            }
            return result
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