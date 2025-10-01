package com.gemwallet.features.earn.delegation.presents.views

import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.DelegationState

@Composable
internal fun DelegationState(state: DelegationState, availableIn: String, listPosition: ListPosition) {
    PropertyItem(
        title = when (state) {
            DelegationState.Activating -> R.string.stake_active_in
            else -> R.string.stake_available_in
        },
        data = availableIn,
        listPosition = listPosition,
    )
}