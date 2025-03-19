package com.gemwallet.android.features.stake.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.features.stake.model.formatApr
import com.gemwallet.android.features.stake.model.getIconUrl
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.space4
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationValidator

@Composable
fun ValidatorItem(
    data: DelegationValidator,
    isSelected: Boolean = false,
    inContainer: Boolean = false,
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
        dividerShowed = !inContainer,
        title = { ListItemTitleText(data.name) },
        trailing = {
            Row {
                ListItemSupportText(R.string.stake_apr, " ${data.formatApr()}")
                if (isSelected) {
                    Spacer(modifier = Modifier.size(space4))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "selected_delegation",
                    )
                }
            }
        },
    )
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
                commision = 0.5,
                apr = 9.10,
            ),
            isSelected = false,
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
                commision = 0.5,
                apr = 9.10,
            ),
            isSelected = true,
            onClick = {},
        )
    }
}