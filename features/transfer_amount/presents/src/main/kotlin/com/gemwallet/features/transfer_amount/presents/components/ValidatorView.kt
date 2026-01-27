package com.gemwallet.features.transfer_amount.presents.components

import androidx.compose.foundation.lazy.LazyListScope
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.ValidatorItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

internal fun LazyListScope.validatorView(
    txType: TransactionType,
    validatorState: DelegationValidator?,
    onValidator: () -> Unit
) {
    validatorState ?: return
    item {
        SubheaderItem(R.string.stake_validator)
    }
    item {
        ValidatorItem(
            data = validatorState,
            listPosition = ListPosition.Single,
            trailingIcon = { DataBadgeChevron() },
            onClick = when (txType) {
                TransactionType.StakeUndelegate -> null
                else -> {
                    { onValidator() }
                }
            }
        )
    }
}