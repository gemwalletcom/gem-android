package com.gemwallet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.Spacer2
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.gemwallet.android.ui.models.PriceState
import com.gemwallet.android.ui.models.PriceUIModel
import com.wallet.core.primitives.Asset

@Composable
fun AssetListItem(
    uiModel: AssetItemUIModel,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leading = { IconWithBadge(uiModel.asset) },
        title = { ListItemTitleText(uiModel.name) },
        subtitle = if (uiModel.price.fiatFormatted.isEmpty()) {
            null
        } else {
            {
                PriceInfo(
                    price = uiModel.price,
                    style = MaterialTheme.typography.bodyMedium,
                    internalPadding = 4.dp
                )
            }
        },
        trailing = getBalanceInfo(uiModel),
    )
}

@Composable
fun AssetListItem(
    uiModel: AssetItemUIModel,
    support: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        leading = { IconWithBadge(uiModel.asset) },
        title = { ListItemTitleText(uiModel.name, { Badge(text = badge) }) },
        subtitle = support,
        dividerShowed = dividerShowed,
        trailing = trailing
    )
}

@Composable
fun AssetListItem(
    asset: Asset,
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    support: String? = null,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        leading = {
            IconWithBadge(asset)
        },
        title = { ListItemTitleText(asset.name, { Badge(text = badge) }) },
        subtitle = if (support.isNullOrEmpty()) null else {
            { ListItemSupportText(support) }
        },
        dividerShowed = dividerShowed,
        trailing = trailing
    )
}

@Composable
fun Badge(text: String?) {
    if (text.isNullOrEmpty()) {
        return
    }
    Spacer(modifier = Modifier.size(4.dp))
    Text(
        modifier = Modifier,
        text = text,
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.W400,
    )
}

@Composable
fun PriceInfo(
    price: PriceUIModel,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    isHighlightPercentage: Boolean = false,
    internalPadding: Dp = 16.dp,
) {
    val color = priceColor(price.state)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = price.fiatFormatted,
            color = if (isHighlightPercentage) color else MaterialTheme.colorScheme.secondary,
            style = style,
        )
        Spacer(modifier = Modifier.width(internalPadding))
        Text(
            modifier = if (isHighlightPercentage) {
                Modifier.background(color.copy(alpha = 0.15f), MaterialTheme.shapes.small)
            } else {
                Modifier
            }.padding(4.dp),
            text = price.percentageFormatted,
            color = color,
            style = style,
        )
    }
}

fun getBalanceInfo(uiModel: AssetItemUIModel): @Composable () -> Unit {
    return (@Composable {
        val color = MaterialTheme.colorScheme.let {
            if (uiModel.isZeroAmount) it.secondary else it.onSurface
        }
        Column(
            modifier = Modifier.defaultMinSize(40.dp),
            horizontalAlignment = Alignment.End
        ) {
            ListItemTitleText(uiModel.cryptoFormatted, color = color)
            if (!uiModel.isZeroAmount) {
                Spacer2()
                ListItemSupportText(uiModel.fiatFormatted)
            }
        }
    })
}

@Composable
fun PriceInfo(
    priceValue: String,
    changedPercentages: String,
    state: PriceState,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    isHighlightPercentage: Boolean = false,
    internalPadding: Dp = 16.dp,
) {
    val highlightColor = priceColor(state)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = priceValue,
            color = if (isHighlightPercentage) highlightColor else color,
            style = style,
        )
        Spacer(modifier = Modifier.width(internalPadding))
        Text(
            modifier = if (isHighlightPercentage) {
                Modifier.background(highlightColor.copy(alpha = 0.15f), MaterialTheme.shapes.small)
            } else {
                Modifier
            }.padding(4.dp),
            text = changedPercentages,
            color = highlightColor,
            style = style,
        )
    }
}

@Composable
fun priceColor(state: PriceState) = when (state) {
    PriceState.Up -> MaterialTheme.colorScheme.tertiary
    PriceState.Down -> MaterialTheme.colorScheme.error
    PriceState.None -> MaterialTheme.colorScheme.secondary
}