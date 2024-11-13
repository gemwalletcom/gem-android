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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.same
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.interactors.getSupportIconUrl
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator10
import com.gemwallet.android.ui.components.titles.getAddress
import com.gemwallet.android.ui.components.titles.getTransactionTitle
import com.gemwallet.android.ui.components.titles.getValue
import com.gemwallet.android.ui.components.titles.getValueColor
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType
import java.math.BigDecimal
import kotlin.String

@Composable
fun TransactionItem(item: TransactionExtended, isLast: Boolean, onTransaction: (String) -> Unit) {
    val value = Crypto(item.transaction.value.toBigInteger())
    TransactionItem(
        assetIcon = item.asset.getIconUrl(),
        supportIcon = item.asset.getSupportIconUrl(),
        assetSymbol = item.asset.symbol,
        to = item.transaction.to,
        from = item.transaction.from,
        direction = item.transaction.direction,
        type = item.transaction.type,
        state = item.transaction.state,
        value = item.asset.format(
            crypto = value,
            decimalPlace = if (value.value(item.asset.decimals) < BigDecimal.ONE) 4 else 2,
            dynamicPlace = true,
        ),
        metadata = item.transaction.getSwapMetadata(),
        assets = item.assets,
        isLast = isLast
    ) { onTransaction(item.transaction.id) }
}

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
                        val toId = swapMetadata?.toAsset
                        val asset = assets.firstOrNull { toId?.same(it.id) == true }
                        if (swapMetadata == null || asset == null) {
                            ""
                        } else {
                            "+${asset.format(Crypto(swapMetadata.toValue), dynamicPlace = true)}"
                        }
                    }
                    else -> type.getValue(direction, value)
                },
                color = when (type) {
                    TransactionType.Swap -> MaterialTheme.colorScheme.tertiary
                    else -> direction.getValueColor()
                },
                subtitle = {
                    val text = when (type) {
                        TransactionType.Swap -> {
                            val swapMetadata = metadata as? TransactionSwapMetadata
                            val fromId = swapMetadata?.fromAsset
                            val asset = assets.firstOrNull { fromId?.same(it.id) == true }
                            if (swapMetadata == null || asset == null) {
                                ""
                            } else {
                                "-${
                                    asset.format(
                                        Crypto(swapMetadata.fromValue),
                                        dynamicPlace = true
                                    )
                                }"
                            }
                        }

                        else -> ""
                    }
                    if (text.isNotEmpty()) {
                        ListItemSupportText(text)
                    }
                },
                horizontalAlignment = Alignment.End,
            )
        },
        body = {
            ListItemTitle(
                title = type.getTransactionTitle(direction, state, assetSymbol = assetSymbol),
                subtitle = { ListItemSupportText(type.getAddress(direction, from, to)) },
                titleBadge = {
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
                            .background(
                                color = color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (badge.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(
                                    start = 5.dp,
                                    top = 2.dp,
                                    end = padding4,
                                    bottom = 2.dp
                                ),
                                text = badge,
                                color = color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
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

@Composable
@Preview
fun PreviewSwapTransactionItem() {
    WalletTheme {
        TransactionItem(
            assetIcon = "",
            assetSymbol = "BNB",
            from = "0xBA4D1d35bCe0e8F28E5a3403e7a0b996c5d50AC4",
            to = "0xBA4D1d35bCe0e8F28E5a3403e7a0b996c5d50AC4",
            direction = TransactionDirection.Outgoing,
            type = TransactionType.Swap,
            state = TransactionState.Confirmed,
            value = "0.9998888999 BTC",
            assets = listOf(
                Asset(
                    id = AssetId(Chain.SmartChain),
                    name = "SmartChain",
                    symbol = "BNB",
                    decimals = 18,
                    type = AssetType.NATIVE,
                ),
                Asset(
                    id = AssetId(Chain.SmartChain, "0x76A797A59Ba2C17726896976B7B3747BfD1d220f"),
                    name = "Ton",
                    symbol = "TON",
                    decimals = 9,
                    type = AssetType.BEP20,
                ),
            ),
            metadata = TransactionSwapMetadata(
                fromAsset = AssetId(Chain.SmartChain),
                toAsset = AssetId(Chain.SmartChain, "0x76A797A59Ba2C17726896976B7B3747BfD1d220f"),
                fromValue = "90000000000000000",
                toValue = "19000000000000",
            ),
            onClick = {},
        )
    }
}
