package com.gemwallet.android.ui.components.list_item

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.PriceUIState
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.space4
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator

@Composable
fun ValidatorItem(
    data: DelegationValidator,
    listPosition: ListPosition,
    isSelected: Boolean = false,
    onClick: ((String) -> Unit)?
) {
    ValidatorItem(
        data = data,
        listPosition = listPosition,
        trailingIcon = {
            if (isSelected) {
                Spacer(modifier = Modifier.size(space4))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "selected_delegation",
                )
            }
        },
        onClick = onClick
    )
}

@Composable
fun ValidatorItem(
    data: DelegationValidator,
    listPosition: ListPosition,
    trailingIcon: @Composable () -> Unit,
    onClick: ((String) -> Unit)?
) {
    ListItem(
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke(data.id) },
        leading = {
            IconWithBadge(
                icon = data.getIconUrl(),
                placeholder = data.name.firstOrNull()?.toString() ?: data.id.firstOrNull()?.toString() ?: "V",
            )
        },
        title = { ListItemTitleText(data.name) },
        listPosition = listPosition,
        trailing = {
            Row (verticalAlignment = Alignment.CenterVertically) {
                ListItemSupportText(R.string.stake_apr, " ${data.formatApr()}")
                Box(modifier = Modifier.padding(bottom = 2.dp)) {
                    trailingIcon()
                }
            }
        },
    )
}

fun DelegationValidator.formatApr(): String {
    return PriceUIState.formatPercentage(apr, showSign = false, showZero = true)
}

fun DelegationValidator.getIconUrl(): String {
    return "https://assets.gemwallet.com/blockchains/${chain.string}/validators/${id}/logo.png"
}

fun availableIn(delegation: Delegation?): String {
    val completionDate = (delegation?.base?.completionDate ?: return "") - System.currentTimeMillis()
    if (completionDate < 0) {
        return "0"
    }
    val days = completionDate / DateUtils.DAY_IN_MILLIS
    val hours = (completionDate % DateUtils.DAY_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS
    val minutes = (completionDate % DateUtils.DAY_IN_MILLIS % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
    val seconds = (completionDate % DateUtils.DAY_IN_MILLIS % DateUtils.HOUR_IN_MILLIS % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS
    return when {
        days > 0 -> "$days days $hours hours"
        hours > 0 -> "$hours hours $minutes minutes"
        else -> "$minutes minutes $seconds seconds"
    }
}

@Composable
@Preview
fun PreviewValidatorItem() {
    WalletTheme {
        ValidatorItem(
            data = DelegationValidator(
                chain = Chain.Sei,
                id = "some_validator_id",
                name = "Castlenode",
                isActive = true,
                commission = 0.5,
                apr = 9.10,
            ),
            isSelected = false,
            listPosition = ListPosition.Middle,
            onClick = {},
        )
    }
}

@Composable
@Preview
fun PreviewValidatorItemSelected() {
    WalletTheme {
        ValidatorItem(
            data = DelegationValidator(
                chain = Chain.Sei,
                id = "some_validator_id",
                name = "Castlenode",
                isActive = true,
                commission = 0.5,
                apr = 9.10,
            ),
            listPosition = ListPosition.Single,
            isSelected = true,
            onClick = {},
        )
    }
}