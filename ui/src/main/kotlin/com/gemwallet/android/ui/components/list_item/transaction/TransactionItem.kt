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
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator10
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

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
        if (text.isEmpty()) {
            return
        }
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

@Composable
@Preview
fun PreviewTransactionItem() {
    MaterialTheme {
        TransactionItem(
            data = object : TransactionDataAggregate {
                override val id = "preview-1"
                override val asset = Asset(
                    id = AssetId(Chain.Bitcoin),
                    name = "Bitcoin",
                    symbol = "BTC",
                    decimals = 8,
                    type = AssetType.NATIVE,
                )
                override val address = "btc12312sdfksdjfks"
                override val value = "-0.9998888999 BTC"
                override val equivalentValue: String? = null
                override val type = TransactionType.Transfer
                override val direction = TransactionDirection.Outgoing
                override val state = TransactionState.Pending
                override val createdAt = System.currentTimeMillis()
            },
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
            data = object : TransactionDataAggregate {
                override val id = "preview-2"
                override val asset = Asset(
                    id = AssetId(Chain.SmartChain),
                    name = "SmartChain",
                    symbol = "BNB",
                    decimals = 18,
                    type = AssetType.NATIVE,
                )
                override val address = "0xBA4D...50AC4"
                override val value = "+19 TON"
                override val equivalentValue = "-0.09 BNB"
                override val type = TransactionType.Swap
                override val direction = TransactionDirection.Outgoing
                override val state = TransactionState.Confirmed
                override val createdAt = System.currentTimeMillis()
            },
            listPosition = ListPosition.Single,
            onClick = {},
        )
    }
}
