package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.models.DelegationBalanceInfoUIModel
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.paddingHalfSmall
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
    assetInfo: AssetInfo,
    delegation: Delegation,
    completedAt: String,
    listPosition: ListPosition,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        listPosition = listPosition,
        leading = {
            IconWithBadge(
                icon = "https://assets.gemwallet.com/blockchains/${delegation.validator.chain.string}/validators/${delegation.validator.id}/logo.png",
                placeholder = delegation.validator.name.firstOrNull()?.toString() ?: delegation.validator.id.firstOrNull()?.toString() ?: "",
            )
        },
        title = {
            val color = when (delegation.base.state) {
                Active -> MaterialTheme.colorScheme.tertiary
                Pending,
                Undelegating,
                Activating,
                Deactivating -> pendingColor
                AwaitingWithdrawal,
                Inactive -> MaterialTheme.colorScheme.error
            }
            ListItemTitleText(
                text = delegation.validator.name,
                titleBadge = {
                    Row(
                        Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                start = 5.dp,
                                top = 2.dp,
                                end = paddingHalfSmall,
                                bottom = 2.dp
                            ),
                            text = when (delegation.base.state) {
                                Active -> stringResource(if (delegation.validator.isActive) R.string.stake_active else R.string.stake_inactive)
                                Pending -> stringResource(id = R.string.stake_pending)
                                Undelegating -> stringResource(id = R.string.transfer_unstake_title)
                                Inactive -> stringResource(id = R.string.stake_inactive)
                                Activating -> stringResource(id = R.string.stake_activating)
                                Deactivating -> stringResource(id = R.string.stake_deactivating)
                                AwaitingWithdrawal -> stringResource(id = R.string.stake_awaiting_withdrawal)
                            },
                            color = color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            )
        },
        subtitle = {
            when (delegation.base.state) {
                Pending,
                Activating,
                Deactivating -> completedAt.takeIf { it.isNotEmpty() && it != "0"}?.let {
                    Spacer2()
                    ListItemSupportText(completedAt)
                }
                Active,
                Undelegating,
                Inactive,
                AwaitingWithdrawal -> null
            }
        },
        trailing = {
            val balance = DelegationBalanceInfoUIModel(
                assetInfo = assetInfo,
                delegation = delegation.base,
            )
            getBalanceInfo(balance, balance).invoke()
        }
    )
}