package com.gemwallet.android.features.stake.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.gemwallet.android.R
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.designsystem.Spacer2
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationState.Activating
import com.wallet.core.primitives.DelegationState.Active
import com.wallet.core.primitives.DelegationState.AwaitingWithdrawal
import com.wallet.core.primitives.DelegationState.Deactivating
import com.wallet.core.primitives.DelegationState.Inactive
import com.wallet.core.primitives.DelegationState.Pending
import com.wallet.core.primitives.DelegationState.Undelegating

@Composable
fun DelegationItem(
    assetDecimals: Int,
    assetSymbol: String,
    delegation: Delegation,
    completedAt: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leading = {
            IconWithBadge(
                icon = "https://assets.gemwallet.com/blockchains/${delegation.validator.chain.string}/validators/${delegation.validator.id}/logo.png",
                placeholder = delegation.validator.name.firstOrNull()?.toString() ?: delegation.validator.id.firstOrNull()?.toString() ?: "",
            )
        },
        title = { ListItemTitleText(delegation.validator.name) },
        subtitle = {
            Text(
                text = when (delegation.base.state) {
                    Active -> stringResource(if (delegation.validator.isActive) R.string.stake_active else R.string.stake_inactive)
                    Pending -> stringResource(id = R.string.stake_pending)
                    Undelegating -> stringResource(id = R.string.transfer_unstake_title)
                    Inactive -> stringResource(id = R.string.stake_inactive)
                    Activating -> stringResource(id = R.string.stake_activating)
                    Deactivating -> stringResource(id = R.string.stake_deactivating)
                    AwaitingWithdrawal -> stringResource(id = R.string.stake_awaiting_withdrawal)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = when (delegation.base.state) {
                    Active -> MaterialTheme.colorScheme.tertiary
                    Pending,
                    Undelegating,
                    Activating,
                    Deactivating -> pendingColor
                    AwaitingWithdrawal,
                    Inactive -> MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailing = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                ListItemTitleText(Crypto(delegation.base.balance).format(assetDecimals, assetSymbol, 2))

                when (delegation.base.state) {
                    Pending,
                    Activating,
                    Deactivating -> {
                        Spacer2()
                        ListItemSupportText(completedAt)
                    }
                    Active,
                    Undelegating,
                    Inactive,
                    AwaitingWithdrawal -> null
                }
            }
        }
    )
}