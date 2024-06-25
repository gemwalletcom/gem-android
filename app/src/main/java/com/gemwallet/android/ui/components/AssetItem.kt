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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain


@Composable
fun AssetListItem(
    chain: Chain,
    title: String,
    assetType: AssetType,
    iconUrl: String,
    value: String,
    isZeroValue: Boolean,
    fiatAmount: String,
    modifier: Modifier = Modifier,
    price: PriceUIState? = null,
    badge: String? = null,
) {
    AssetListItem(
        modifier = modifier,
        chain = chain,
        title = title,
        assetType = assetType,
        iconUrl = iconUrl,
        price = price,
        trailing = getBalanceInfo(isZeroValue, value, fiatAmount),
        badge = badge,
    )
}

@Composable
fun AssetListItem(
    chain: Chain,
    title: String,
    assetType: AssetType,
    iconUrl: String,
    modifier: Modifier = Modifier,
    price: PriceUIState? = null,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        iconUrl = iconUrl,
        supportIcon = if (assetType == AssetType.NATIVE) null else chain.getIconUrl(),
        placeholder = title[0].toString(),
        trailing = trailing
    ) {
        val priceInfo: (@Composable () -> Unit)? = if (price == null || price.value.isEmpty()) {
            null
        } else {
            {
                PriceInfo(
                    price = price,
                    style = MaterialTheme.typography.bodyMedium,
                    internalPadding = 4.dp
                )
            }
        }
        ListItemTitle(
            modifier = Modifier.fillMaxHeight(),
            title = title,
            titleBadge = { Badge(text = badge) },
            subtitle = priceInfo,
        )
    }
}

@Composable
fun AssetListItem(
    chain: Chain,
    title: String,
    support: String?,
    assetType: AssetType,
    iconUrl: String,
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        iconUrl = iconUrl,
        supportIcon = if (assetType == AssetType.NATIVE) null else chain.getIconUrl(),
        placeholder = title[0].toString(),
        dividerShowed = dividerShowed,
        trailing = trailing
    ) {
        ListItemTitle(
            modifier = Modifier.fillMaxHeight(),
            title = title,
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
    price: PriceUIState,
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
            text = price.value,
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
            text = price.dayChanges,
            color = color,
            style = style,
        )
    }
}

fun getBalanceInfo(isZeroValue: Boolean, value: String, fiatAmount: String): @Composable () -> Unit {
    return (@Composable {
        if (isZeroValue) {
            ListItemTitle(
                modifier = Modifier.defaultMinSize(minHeight = 40.dp),
                title = value,
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "",
                horizontalAlignment = Alignment.End,
            )
        } else {
            ListItemTitle(
                title = value,
                color = MaterialTheme.colorScheme.onSurface,
                subtitle = fiatAmount,
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

@Preview
@Composable
fun PreviewAssetListItem() {
    WalletTheme {
        AssetListItem(
            chain = Chain.SmartChain,
            title = "Foo Asset",
            assetType = AssetType.BEP20,
            badge = "BNB",
            iconUrl = "https://icon.net",
            price = PriceUIState(
                value = "88 0000$",
                dayChanges = "1000%",
                state = PriceState.Up,
            )
        )
    }
}

@Preview
@Composable
fun PreviewAssetListItemWithBalance() {
    WalletTheme {
        AssetListItem(
            chain = Chain.SmartChain,
            title = "Foo Asset",
            assetType = AssetType.BEP20,
            iconUrl = "https://icon.net",
            isZeroValue = false,
            value = "10 BNB",
            fiatAmount = "10000$",
            price = PriceUIState(
                value = "88 0000$",
                dayChanges = "1000%",
                state = PriceState.Up,
            )
        )
    }
}