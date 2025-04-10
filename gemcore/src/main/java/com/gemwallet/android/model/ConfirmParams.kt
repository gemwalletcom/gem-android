package com.gemwallet.android.model

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.type
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.AccountSerializer
import com.gemwallet.android.serializer.AssetIdSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.TransactionType
import uniffi.gemstone.ApprovalData
import java.math.BigInteger
import java.util.Base64

sealed class ConfirmParams(
    val asset: Asset,
    val from: Account,
    val amount: BigInteger = BigInteger.ZERO,
) {

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

    sealed class TransferParams(
        asset: Asset,
        from: Account,
        amount: BigInteger,
        val destination: DestinationAddress,
        val memo: String? = null,
        val isMaxAmount: Boolean = false,
    ) : ConfirmParams(asset, from, amount) {

        override fun isMax(): Boolean {
            return isMaxAmount
        }

        override fun destination(): DestinationAddress {
            return destination
        }

        override fun memo(): String? {
            return memo
        }

        class Native(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            destination: DestinationAddress,
            memo: String? = null,
            isMaxAmount: Boolean = false,
        ) : TransferParams(asset, from, amount, destination, memo, isMaxAmount)

        class Token(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            destination: DestinationAddress,
            memo: String? = null,
            isMaxAmount: Boolean = false,
        ) : TransferParams(asset, from, amount, destination, memo, isMaxAmount)
    }

    class TokenApprovalParams(
        asset: Asset,
        from: Account,
        val data: String,
        val provider: String,
        val contract: String
    ) : ConfirmParams(asset, from)

    class SwapParams(
        from: Account,
        val fromAsset: Asset,
        val fromAmount: BigInteger,
        val toAssetId: AssetId,
        val toAmount: BigInteger,
        val swapData: String,
        val provider: String,
        val to: String,
        val value: String,
        val approval: ApprovalData? = null,
        val gasLimit: BigInteger? = null,
    ) : ConfirmParams(fromAsset, from, fromAmount) {

        override fun destination(): DestinationAddress = DestinationAddress(to)

    }

    class Activate(
        asset: Asset,
        from: Account,
    ) : ConfirmParams(asset = asset, from = from)

    sealed class Stake(
        asset: Asset,
        from: Account,
        amount: BigInteger,
        val validatorId: String,
    ) : ConfirmParams(asset, from, amount) {

        class DelegateParams(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            validatorId: String,
        ) : Stake(asset, from, amount, validatorId)

        class WithdrawParams(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            validatorId: String,
            val delegationId: String,
        ) : Stake(asset, from, amount, validatorId)

        class UndelegateParams(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            validatorId: String,
            val delegationId: String,
            val share: String?,
            val balance: String?
        ) : Stake(asset, from, amount, validatorId)

        class RedelegateParams(
            asset: Asset,
            from: Account,
            amount: BigInteger,
            val srcValidatorId: String,
            val dstValidatorId: String,
            val share: String?,
            val balance: String?
        ) : Stake(asset, from, amount, srcValidatorId)

        class RewardsParams(
            asset: Asset,
            from: Account,
            val validatorsId: List<String>,
            amount: BigInteger
        ) : Stake(asset, from, amount, "")
    }

    fun pack(): String? {
        val json = getGson().toJson(this)
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
            val type = when (txType) {
                TransactionType.Transfer -> TransferParams.Native::class.java
                TransactionType.Swap -> SwapParams::class.java
                TransactionType.TokenApproval -> TokenApprovalParams::class.java
                TransactionType.StakeDelegate -> Stake.DelegateParams::class.java
                TransactionType.StakeUndelegate -> Stake.UndelegateParams::class.java
                TransactionType.StakeRewards -> Stake.RewardsParams::class.java
                TransactionType.StakeRedelegate -> Stake.RedelegateParams::class.java
                TransactionType.StakeWithdraw -> Stake.WithdrawParams::class.java
                TransactionType.AssetActivation -> Activate::class.java
                TransactionType.TransferNFT -> TODO()
                TransactionType.SmartContractCall -> TODO()
            }

            val result = getGson().fromJson(json, type)

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

        private fun getGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
                .registerTypeAdapter(Account::class.java, AccountSerializer())
                .create()
        }
    }
}