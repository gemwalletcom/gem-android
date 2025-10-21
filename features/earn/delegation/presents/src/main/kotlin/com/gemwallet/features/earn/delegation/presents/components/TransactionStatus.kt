package com.gemwallet.features.earn.delegation.presents.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.DelegationState

@Composable
internal fun TransactionStatus(state: DelegationState, active: Boolean, listPosition: ListPosition) {
    PropertyItem(
        title = R.string.transaction_status,
        data = when (state) {
            DelegationState.Active -> when (active) {
                true -> R.string.stake_active
                false -> R.string.stake_inactive
            }

            DelegationState.Pending -> R.string.stake_pending
            DelegationState.Inactive -> R.string.stake_inactive
            DelegationState.Activating -> R.string.stake_activating
            DelegationState.Deactivating -> R.string.stake_deactivating
            DelegationState.AwaitingWithdrawal -> R.string.stake_awaiting_withdrawal
        },
        dataColor = when (state) {
            DelegationState.Active -> MaterialTheme.colorScheme.tertiary
            DelegationState.Activating,
            DelegationState.Deactivating,
            DelegationState.Pending -> pendingColor
            DelegationState.AwaitingWithdrawal,
            DelegationState.Inactive -> MaterialTheme.colorScheme.error
        },
        listPosition = listPosition
    )
}