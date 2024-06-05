package com.gemwallet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.ui.components.titles.getAddress
import com.gemwallet.android.ui.components.titles.getTransactionTitle
import com.gemwallet.android.ui.components.titles.getValue
import com.gemwallet.android.ui.components.titles.getValueColor
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding4
import com.gemwallet.android.ui.theme.padding8
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionItem(
    assetIcon: String,
    assetSymbol: String,
    type: TransactionType,
    state: TransactionState,
    value: String,
    from: String,
    to: String,
    direction: TransactionDirection,
    metadata: Any?,
    assets: List<Asset>,
    supportIcon: String? = null,
    isLast: Boolean = false,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .heightIn(72.dp),
        iconUrl = assetIcon,
        supportIcon = supportIcon,
        placeholder = assetSymbol,
        dividerShowed = !isLast,
        trailing = {
            ListItemTitle(
                title = when (type) {
                    TransactionType.Swap -> {
                        val swapMetadata = metadata as? TransactionSwapMetadata
                        val toAssetId = swapMetadata?.toAsset?.toIdentifier()
                        val asset = assets.firstOrNull { it.id.toIdentifier() == toAssetId }
                        if (swapMetadata == null || asset == null) {
                            ""
                        } else {
                            "+${Crypto(swapMetadata.toValue)
                                    .format(asset.decimals, asset.symbol, 2, dynamicPlace = true)}"
                        }
                    }
                    else -> type.getValue(direction, value)
                },
                color = when (type) {
                    TransactionType.Swap -> MaterialTheme.colorScheme.tertiary
                    else -> direction.getValueColor()
                },
                subtitle = when (type) {
                    TransactionType.Swap -> {
                        val swapMetadata = metadata as? TransactionSwapMetadata
                        val toAssetId = swapMetadata?.fromAsset?.toIdentifier()
                        val asset = assets.firstOrNull { it.id.toIdentifier() == toAssetId }
                        if (swapMetadata == null || asset == null) {
                            ""
                        } else {
                            "-${Crypto(swapMetadata.fromValue)
                                .format(asset.decimals, asset.symbol, 2, dynamicPlace = true)}"
                        }
                    }
                    else -> ""
                },
                horizontalAlignment = Alignment.End,
            )
        },
        body = {
            ListItemTitle(
                title = type.getTransactionTitle(assetSymbol = assetSymbol),
                subtitle = type.getAddress(direction, from, to),
                titleBudge = {
                    val badge = when (state) {
                        TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
                        TransactionState.Confirmed -> ""
                        TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
                        TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
                    }
                    val color = when (state) {
                        TransactionState.Pending -> pendingColor
                        TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
                        TransactionState.Reverted,
                        TransactionState.Failed -> MaterialTheme.colorScheme.error
                    }
                    Row(
                        Modifier
                            .padding(start = 5.dp)
                            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (badge.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(start = 5.dp, top = 2.dp, end = padding4, bottom = 2.dp),
                                text = badge,
                                color = color,
                                style = MaterialTheme.typography.labelMedium
                            )
                            if (state == TransactionState.Pending) {
                                CircularProgressIndicator10(color = color)
                                Spacer8()
                            }
                        }
                    }
                }
            )
        },
    )
}

@Composable
@Preview
fun PreviewTransactionItem() {
    WalletTheme {
        TransactionItem(
            assetIcon = "",
            assetSymbol = "BTC",
            from = "btc12312sdfksdjfks",
            to = "btc12312sdfksdjfks",
            direction = TransactionDirection.Outgoing,
            type = TransactionType.Transfer,
            state = TransactionState.Pending,
            value = "0.9998888999 BTC",
            metadata = null,
            assets = emptyList(),
            onClick = {},
        )
    }
}
