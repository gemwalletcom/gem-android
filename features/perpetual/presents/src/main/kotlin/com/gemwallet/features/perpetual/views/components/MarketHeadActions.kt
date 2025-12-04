package com.gemwallet.features.perpetual.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountHeadAction
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun MarketHeadActions(
    onWithdraw: () -> Unit,
    onDeposit: () -> Unit,
) {
    var actionFontSize by remember { mutableStateOf(16.sp) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(paddingDefault),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AmountHeadAction(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.wallet_withdraw),
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "withdraw",
            fontSize = actionFontSize,
            onNextFontSize = {
                if (actionFontSize > it) actionFontSize = it
            },
            onClick = onWithdraw,
        )
        AmountHeadAction(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.wallet_deposit),
            imageVector = Icons.Default.Add,
            contentDescription = "deposit",
            fontSize = actionFontSize,
            onNextFontSize = {
                if (actionFontSize > it) actionFontSize = it
            },
            onClick = onDeposit,
        )
    }
}

@Preview
@Composable
private fun MarketHeadActionsPreview() {
    WalletTheme {
        MarketHeadActions(
            onWithdraw = {},
            onDeposit = {}
        )
    }
}