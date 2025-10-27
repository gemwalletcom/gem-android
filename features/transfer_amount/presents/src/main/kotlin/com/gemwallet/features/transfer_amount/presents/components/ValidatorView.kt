package com.gemwallet.features.transfer_amount.presents.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.gemwallet.android.ui.components.list_item.ValidatorItem
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
        ValidatorItem(
            data = validatorState,
            listPosition = ListPosition.Single,
            trailingIcon = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "select_validator",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            onClick = when (txType) {
                TransactionType.StakeUndelegate -> null
                else -> {
                    { onValidator() }
                }
            }
        )
    }
}