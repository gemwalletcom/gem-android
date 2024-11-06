package com.gemwallet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.interactors.getSupportIconUrl
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.gemwallet.android.ui.models.PriceState
import com.gemwallet.android.ui.models.PriceUIModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

@Composable
fun AssetListItem(
    uiModel: AssetItemUIModel,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        iconModifier = iconModifier,
        iconUrl = uiModel.assetIconUrl,
        supportIcon = uiModel.assetNetworkIconUrl,
        placeholder = uiModel.name[0].toString(),
        trailing = getBalanceInfo(uiModel),
    ) {
        val priceInfo: (@Composable () -> Unit)? = if (uiModel.price.fiatFormatted.isEmpty()) {
            null
        } else {
            {
                PriceInfo(
                    price = uiModel.price,
                    style = MaterialTheme.typography.bodyMedium,
                    internalPadding = 4.dp
                )
            }
        }
        ListItemTitle(
            modifier = Modifier.fillMaxHeight(),
            title = uiModel.name,
            subtitle = priceInfo,
        )
    }
}

@Composable
fun AssetListItem(
    uiModel: AssetItemUIModel,
    support: String?,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        iconModifier = iconModifier,
        iconUrl = uiModel.assetIconUrl,
        supportIcon = uiModel.assetNetworkIconUrl,
        placeholder = uiModel.name[0].toString(),
        dividerShowed = dividerShowed,
        trailing = trailing
    ) {
        ListItemTitle(
            modifier = Modifier.fillMaxHeight(),
            title = uiModel.name,
            titleBudge = { Badge(text = badge) },
            subtitle = support,
        )
    }
}

@Composable
fun AssetListItem(
    asset: Asset,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    support: String? = null,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        iconModifier = iconModifier,
        iconUrl = asset.getIconUrl(),
        supportIcon = asset.getSupportIconUrl(),
        placeholder = asset.name[0].toString(),
        dividerShowed = dividerShowed,
        trailing = trailing
    ) {
        ListItemTitle(
            modifier = Modifier.fillMaxHeight(),
            title = asset.name,
            titleBudge = { Badge(text = badge) },
            subtitle = support,
        )
    }
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
        if (uiModel.isZeroAmount) {
            ListItemTitle(
                modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                title = uiModel.cryptoFormatted,
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "",
                horizontalAlignment = Alignment.End,
            )
        } else {
            ListItemTitle(
                title = uiModel.cryptoFormatted,
                color = MaterialTheme.colorScheme.onSurface,
                subtitle = uiModel.fiatFormatted,
                horizontalAlignment = Alignment.End
            )
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