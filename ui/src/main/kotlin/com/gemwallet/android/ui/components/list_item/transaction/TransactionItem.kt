package com.gemwallet.android.ui.components.list_item.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator10
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionSwapMetadata
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionItem(
    item: TransactionExtended,
    listPosition: ListPosition,
    onTransaction: (String) -> Unit
) {
    val value = Crypto(item.transaction.value.toBigInteger())
    TransactionItem(
        asset = item.asset,
        to = item.transaction.to,
        from = item.transaction.from,
        direction = item.transaction.direction,
        type = item.transaction.type,
        state = item.transaction.state,
        value = item.asset.format(
            crypto = value,
            dynamicPlace = true,
        ),
        metadata = item.transaction.getSwapMetadata(),
        assets = item.assets,
        listPosition = listPosition,
    ) { onTransaction(item.transaction.id) }
}

@Composable
fun TransactionItem(
    data: TransactionDataAggregate,
    listPosition: ListPosition,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick).heightIn(72.dp),
        leading = { IconWithBadge(asset = data.asset) },
        title = {
            ListItemTitleText(
                text = data.getTitle(),
                titleBadge = { TransactionStatusBadge(data) }
            )
        },
        subtitle = data.formatAddress()?.let { { ListItemSupportText(it) } },
        listPosition = listPosition,
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                ListItemTitleText(
                    text = data.value,
                    color = data.getValueColor(),
                )
                data.equivalentValue?.let {
                    Spacer2()
                    ListItemSupportText(it)
                }
            }
        }
    )
}

@Composable
private fun TransactionStatusBadge(data: TransactionDataAggregate) {
    val text = data.getBadgeText()
    val color = data.getBadgeColor()
    Row(
        Modifier
            .padding(start = 5.dp)
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(
                    start = 5.dp,
                    top = 2.dp,
                    end = paddingHalfSmall,
                    bottom = 2.dp
                ),
                text = text,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
            if (data.isPending) {
                CircularProgressIndicator10(color = color)
                Spacer8()
            }
        }
    }
}

@Composable
fun TransactionItem(
    asset: Asset,
    type: TransactionType,
    state: TransactionState,
    value: String,
    from: String,
    to: String,
    direction: TransactionDirection,
    metadata: Any?,
    assets: List<Asset>,
    listPosition: ListPosition,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick).heightIn(72.dp),
        leading = { IconWithBadge(asset = asset) },
        title = {
            ListItemTitleText(
                text = stringResource(type.getTitle(direction, state)),
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
                                    end = paddingHalfSmall,
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
        subtitle = type.getAddress(direction, from, to).let {
            if (it.isNotEmpty()) {
                { ListItemSupportText(it) }
            } else null
        },
        listPosition = listPosition,
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                ListItemTitleText(
                    text = when (type) {
                        TransactionType.Swap -> {
                            val swapMetadata = metadata as? TransactionSwapMetadata
                            val toId = swapMetadata?.toAsset
                            val asset = assets.firstOrNull { toId == it.id }
                            if (swapMetadata == null || asset == null) {
                                ""
                            } else {
                                "+${
                                    asset.format(
                                        Crypto(swapMetadata.toValue),
                                        dynamicPlace = true
                                    )
                                }"
                            }
                        }

                        else -> type.getValue(direction, value)
                    },
                    color = when (type) {
                        TransactionType.Swap -> MaterialTheme.colorScheme.tertiary
                        else -> direction.getValueColor()
                    },
                )
                val text = when (type) {
                    TransactionType.Swap -> {
                        val swapMetadata = metadata as? TransactionSwapMetadata
                        val fromId = swapMetadata?.fromAsset
                        val asset = assets.firstOrNull { fromId == it.id }
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
                    Spacer2()
                    ListItemSupportText(text)
                }
            }
        }
    )
}

@Composable
fun TransactionDirection.getValueColor(): Color {
    return when (this) {
        TransactionDirection.SelfTransfer,
        TransactionDirection.Outgoing -> MaterialTheme.colorScheme.onSurface
        TransactionDirection.Incoming -> MaterialTheme.colorScheme.tertiary
    }
}

fun TransactionType.getValue(direction: TransactionDirection, value: String): String {
    return when (this) {
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.StakeDelegate,
        TransactionType.PerpetualOpenPosition,
        TransactionType.StakeFreeze,
        TransactionType.StakeUnfreeze,
        TransactionType.PerpetualClosePosition -> value
        TransactionType.Transfer,
        TransactionType.Swap -> when (direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> "-${value}"
            TransactionDirection.Incoming -> "+${value}"
        }
        TransactionType.TokenApproval,
        TransactionType.TransferNFT,
        TransactionType.AssetActivation,
        TransactionType.SmartContractCall,
        TransactionType.PerpetualModifyPosition
             -> ""
    }
}

@Composable
fun TransactionType.getAddress(direction: TransactionDirection, from: String, to: String): String {
    return when (this) {
        TransactionType.TransferNFT,
        TransactionType.Transfer -> when (direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> "${stringResource(id = R.string.transfer_to)} ${to.getAddressEllipsisText()}"
            TransactionDirection.Incoming -> "${stringResource(id = R.string.transfer_from)} ${from.getAddressEllipsisText()}"
        }
        TransactionType.Swap,
        TransactionType.TokenApproval,
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.AssetActivation,
        TransactionType.StakeRewards,
        TransactionType.SmartContractCall,
        TransactionType.PerpetualOpenPosition,
        TransactionType.StakeFreeze,
        TransactionType.StakeUnfreeze,
        TransactionType.PerpetualClosePosition,
        TransactionType.PerpetualModifyPosition
            -> ""
    }
}

@Composable
@Preview
fun PreviewTransactionItem() {
    MaterialTheme {
        TransactionItem(
            asset = Asset(
                id = AssetId(Chain.Bitcoin),
                name = "Bitcoin",
                symbol = "BTC",
                decimals = 8,
                type = AssetType.NATIVE,
            ),
            from = "btc12312sdfksdjfks",
            to = "btc12312sdfksdjfks",
            direction = TransactionDirection.Outgoing,
            type = TransactionType.Transfer,
            state = TransactionState.Pending,
            value = "0.9998888999 BTC",
            metadata = null,
            assets = emptyList(),
            listPosition = ListPosition.Single,
            onClick = {},
        )
    }
}

@Composable
@Preview
fun PreviewSwapTransactionItem() {
    MaterialTheme {
        TransactionItem(
            asset = Asset(
                id = AssetId(Chain.SmartChain),
                name = "SmartChain",
                symbol = "BNB",
                decimals = 18,
                type = AssetType.NATIVE,
            ),
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
            listPosition = ListPosition.Single,
            onClick = {},
        )
    }
}
