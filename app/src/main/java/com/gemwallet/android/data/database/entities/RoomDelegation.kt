package com.gemwallet.android.data.database.entities

import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.Price

data class RoomDelegation(
    val validatorId: String,
    val delegationId: String,
    val assetId: String,
    val chain: Chain,
    val name: String,
    val isActive: Boolean,
    val commission: Double,
    val apr: Double,
    val address: String,
    val state: DelegationState,
    val balance: String,
    val rewards: String,
    val completionDate: Long? = null,
    val price: Double? = null,
    val priceChange: Double? = null,
    val shares: String? = null,
) {
    fun toModel(): Delegation? {
        return Delegation(
            validator = DelegationValidator(
                id = validatorId,
                chain = chain,
                name = name,
                isActive = isActive,
                commision = commission,
                apr = apr
            ),
            base = DelegationBase(
                assetId = assetId.toAssetId() ?: return null,
                validatorId = validatorId,
                delegationId = delegationId,
                state = state,
                balance = balance,
                completionDate = completionDate,
                rewards = rewards,
                shares = shares ?: "",
            ),
            price = if (price != null) {
                Price(price, priceChange ?: 0.0)
            } else {
                null
            }
        )
    }
}