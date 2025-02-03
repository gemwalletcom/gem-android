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
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.TransactionType
import java.math.BigInteger
import java.util.Base64

sealed class ConfirmParams(
    val assetId: AssetId,
    val from: Account,
    val amount: BigInteger = BigInteger.ZERO,
) {

    class Builder(
        val assetId: AssetId,
        val from: Account,
        val amount: BigInteger = BigInteger.ZERO,
    ) {
        fun transfer(destination: DestinationAddress, memo: String? = null, isMax: Boolean = false): TransferParams {
            return when (assetId.type()) {
                AssetSubtype.NATIVE -> TransferParams.Native(
                    assetId = assetId,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
                AssetSubtype.TOKEN -> TransferParams.Token(
                    assetId = assetId,
                    from = from,
                    amount = amount,
                    destination = destination,
                    memo = memo,
                    isMaxAmount = isMax
                )
            }
        }

        fun approval(approvalData: String, provider: String): TokenApprovalParams {
            return TokenApprovalParams(assetId, from, approvalData, provider, contract = "")
        }

        fun delegate(validatorId: String) = Stake.DelegateParams(assetId, from, amount, validatorId)

        fun rewards(validatorsId: List<String>) = Stake.RewardsParams(assetId, from, validatorsId, amount)

        fun withdraw(delegation: Delegation) = Stake.WithdrawParams(
            assetId = assetId,
            from = from,
            amount = amount,
            validatorId = delegation.validator.id,
            delegationId = delegation.base.delegationId,
        )

        fun undelegate(delegation: Delegation): Stake.UndelegateParams {
            return Stake.UndelegateParams(
                assetId,
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
                assetId,
                from = from,
                amount,
                delegation.validator.id,
                dstValidatorId,
                delegation.base.shares,
                delegation.base.balance,
            )
        }
    }

    sealed class TransferParams(
        assetId: AssetId,
        from: Account,
        amount: BigInteger,
        val destination: DestinationAddress,
        val memo: String? = null,
        val isMaxAmount: Boolean = false,
    ) : ConfirmParams(assetId, from, amount) {

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
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            destination: DestinationAddress,
            memo: String? = null,
            isMaxAmount: Boolean = false,
        ) : TransferParams(assetId, from, amount, destination, memo, isMaxAmount)

        class Token(
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            destination: DestinationAddress,
            memo: String? = null,
            isMaxAmount: Boolean = false,
        ) : TransferParams(assetId, from, amount, destination, memo, isMaxAmount)
    }

    class TokenApprovalParams(
        assetId: AssetId,
        from: Account,
        val data: String,
        val provider: String,
        val contract: String
    ) : ConfirmParams(assetId, from)

    class SwapParams(
        from: Account,
        val fromAssetId: AssetId,
        val fromAmount: BigInteger,
        val toAssetId: AssetId,
        val toAmount: BigInteger,
        val swapData: String,
        val provider: String,
        val to: String,
        val value: String,
    ) : ConfirmParams(fromAssetId, from, fromAmount) {

        override fun destination(): DestinationAddress = DestinationAddress(to)

    }

    sealed class Stake(
        assetId: AssetId,
        from: Account,
        amount: BigInteger,
        val validatorId: String,
    ) : ConfirmParams(assetId, from, amount) {

        class DelegateParams(
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            validatorId: String,
        ) : Stake(assetId, from, amount, validatorId)

        class WithdrawParams(
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            validatorId: String,
            val delegationId: String,
        ) : Stake(assetId, from, amount, validatorId)

        class UndelegateParams(
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            validatorId: String,
            val delegationId: String,
            val share: String?,
            val balance: String?
        ) : Stake(assetId, from, amount, validatorId)

        class RedelegateParams(
            assetId: AssetId,
            from: Account,
            amount: BigInteger,
            val srcValidatorId: String,
            val dstValidatorId: String,
            val share: String?,
            val balance: String?
        ) : Stake(assetId, from, amount, srcValidatorId)

        class RewardsParams(
            assetId: AssetId,
            from: Account,
            val validatorsId: List<String>,
            amount: BigInteger
        ) : Stake(assetId, from, amount, "")
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
        return assetId.toIdentifier().hashCode() +
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
                TransactionType.AssetActivation -> TODO()
                TransactionType.TransferNFT -> TODO()
                TransactionType.SmartContractCall -> TODO()
            }

            val result = getGson().fromJson(json, type)

            return if (result.assetId.type() == AssetSubtype.TOKEN && result is TransferParams.Native) {
                TransferParams.Token(
                    result.assetId,
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