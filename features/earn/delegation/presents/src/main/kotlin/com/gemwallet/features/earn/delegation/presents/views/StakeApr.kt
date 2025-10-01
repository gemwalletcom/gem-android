package com.gemwallet.features.earn.delegation.presents.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.formatApr
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.DelegationValidator

@Composable
internal fun StakeApr(validator: DelegationValidator, listPosition: ListPosition) {
    PropertyItem(
        title = stringResource(R.string.stake_apr, ""),
        data = validator.formatApr(),
        dataColor = when (validator.isActive) {
            true -> MaterialTheme.colorScheme.tertiary
            false -> MaterialTheme.colorScheme.secondary
        },
        listPosition = listPosition,
    )
}