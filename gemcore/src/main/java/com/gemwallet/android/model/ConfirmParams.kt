package com.gemwallet.android.model

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.serializer.AssetIdSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.TransactionType
import java.math.BigInteger
import java.util.Base64

sealed class ConfirmParams(
    val assetId: AssetId,
    val amount: BigInteger = BigInteger.ZERO,
    val domainName: String? = null,
) {

    class Builder(
        val assetId: AssetId,
        val amount: BigInteger = BigInteger.ZERO,
        val domainName: String? = null,
    ) {
        fun transfer(to: String, memo: String? = null, domainName: String?, isMax: Boolean = false): TransferParams {
            return TransferParams(
                assetId = assetId,
                amount = amount,
                to = to,
                domainName = domainName,
                memo = memo,
                isMaxAmount = isMax
            )
        }

        fun approval(approvalData: String, provider: String): TokenApprovalParams {
            return TokenApprovalParams(assetId, approvalData, provider)
        }

        fun delegate(validatorId: String,) = DelegateParams(assetId, amount, validatorId)

        fun rewards(validatorsId: List<String>) = RewardsParams(assetId, validatorsId)

        fun withdraw(delegation: Delegation) = WithdrawParams(
            assetId = assetId,
            amount = amount,
            validatorId = delegation.validator.id,
            delegationId = delegation.base.delegationId,
        )

        fun undelegate(delegation: Delegation): UndelegateParams {
            return UndelegateParams(
                assetId,
                amount,
                delegation.validator.id,
                delegation.base.delegationId,
                delegation.base.shares,
                delegation.base.balance
            )
        }

        fun redelegate(dstValidatorId: String, delegation: Delegation): RedeleateParams {
            return RedeleateParams(
                assetId,
                amount,
                delegation.validator.id,
                dstValidatorId,
                delegation.base.shares,
                delegation.base.balance,
            )
        }
    }

    class TransferParams(
        assetId: AssetId,
        amount: BigInteger,
        val to: String,
        domainName: String? = null,
        val memo: String? = null,
        val isMaxAmount: Boolean = false,
    ) : ConfirmParams(assetId, amount, domainName) {

        override fun isMax(): Boolean {
            return isMaxAmount
        }

        override fun destination(): String {
            return to
        }

        override fun memo(): String? {
            return memo
        }
    }

    class TokenApprovalParams(
        assetId: AssetId,
        val approvalData: String,
        val provider: String,
    ) : ConfirmParams(assetId)

    class SwapParams(
        val fromAssetId: AssetId,
        val fromAmount: BigInteger,
        val toAssetId: AssetId,
        val toAmount: BigInteger,
        val swapData: String,
        val provider: String,
        val to: String,
        val value: String,
    ) : ConfirmParams(fromAssetId, fromAmount) {

        override fun destination(): String = to

    }

    class DelegateParams(
        assetId: AssetId,
        amount: BigInteger,
        val validatorId: String,
    ) : ConfirmParams(assetId, amount)

    class WithdrawParams(
        assetId: AssetId,
        amount: BigInteger,
        val validatorId: String,
        val delegationId: String,
    ) : ConfirmParams(assetId, amount)

    class UndelegateParams(
        assetId: AssetId,
        amount: BigInteger,
        val validatorId: String,
        val delegationId: String,
        val share: String?,
        val balance: String?
    ) : ConfirmParams(assetId, amount)

    class RedeleateParams(
        assetId: AssetId,
        amount: BigInteger,
        val srcValidatorId: String,
        val dstValidatorId: String,
        val share: String?,
        val balance: String?
    ) : ConfirmParams(assetId, amount)

    class RewardsParams(
        assetId: AssetId,
        val validatorsId: List<String>,
    ) : ConfirmParams(assetId)

    fun pack(): String? {
        val json = getGson().toJson(this)
        return Base64.getEncoder().encodeToString(json.toByteArray()).urlEncode()
    }

    fun getTxType() : TransactionType {
        return when (this) {
            is TransferParams  -> TransactionType.Transfer
            is TokenApprovalParams  -> TransactionType.TokenApproval
            is SwapParams  -> TransactionType.Swap
            is DelegateParams  -> TransactionType.StakeDelegate
            is RewardsParams -> TransactionType.StakeRewards
            is RedeleateParams  -> TransactionType.StakeRedelegate
            is UndelegateParams  -> TransactionType.StakeUndelegate
            is WithdrawParams  -> TransactionType.StakeWithdraw
        }
    }

    open fun destination(): String = ""

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
        fun <T : ConfirmParams> unpack(type: Class<T>, input: String): T? {
            val json = String(Base64.getDecoder().decode(input.urlDecode()))
            return getGson().fromJson(json, type)
        }

        private fun getGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(AssetId::class.java, AssetIdSerializer())
                .create()
        }
    }
}