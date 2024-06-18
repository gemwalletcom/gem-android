package com.gemwallet.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.headerIconSize
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.space4
import com.wallet.core.primitives.WalletType
import uniffi.Gemstone.Config
import uniffi.Gemstone.DocsUrl

@Composable
fun AmountListHead(
    amount: String,
    equivalent: String? = null,
    iconUrl: String? = null,
    supportIconUrl: String? = null,
    placeholder: String? = null,
    changedValue: String? = null,
    changedPercentages: String? = null,
    changeState: PriceState = PriceState.None,
    actions: (@Composable () -> Unit)? = null,
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding16, end = padding16, bottom = padding16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderIcon(iconUrl = iconUrl, supportIconUrl = supportIconUrl, placeholder = placeholder)
            Spacer16()
            DisplayText(text = amount, modifier = Modifier.fillMaxWidth())
            if (!equivalent.isNullOrEmpty()) {
                Spacer(modifier = Modifier.size(space4))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = equivalent,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W400,
                )
            }
            if (changedValue != null) {
                Spacer16()
                PriceInfo(
                    priceValue = changedValue,
                    changedPercentages = changedPercentages ?: "",
                    state = changeState,
                    isHighlightPercentage = true,
                )
            }
            if (actions != null) {
                Spacer16()
                actions()
            }
        }
        Spacer(modifier = Modifier.size(14.dp))
        HorizontalDivider(thickness = 0.4.dp)
    }
}

@Composable
internal fun HeaderIcon(
    iconUrl: String?,
    supportIconUrl: String? = null,
    placeholder: String?,
    iconSize: Dp = headerIconSize,
) {
    if (iconUrl == null) {
        return
    }
    Box {
        AsyncImage(
            modifier = Modifier.size(iconSize),
            model = iconUrl,
            placeholderText = placeholder,
            contentDescription = "header_icon"
        )
        if (supportIconUrl != null) {
            AsyncImage(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .border(0.5.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape),
                model = supportIconUrl,
                placeholderText = placeholder,
                contentDescription = "header_support_icon"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AssetHeadActions(
    walletType: WalletType,
    onTransfer: (() -> Unit)?,
    transferEnabled: Boolean,
    onReceive: (() -> Unit)?,
    onBuy: (() -> Unit)?,
    onSwap: (() -> Unit)?,
) {
    if (walletType == WalletType.view) {
        AssetWatchOnly()
        return
    }
    val windowSizeClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onTransfer != null) {
            AssetAction(
                modifier = if (windowSizeClass == WindowWidthSizeClass.Compact) Modifier.weight(1f) else Modifier,
                title = stringResource(id = R.string.wallet_send),
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "send",
                enabled = transferEnabled,
                onClick = onTransfer,
            )
        }
        if (onReceive != null) {
            AssetAction(
                modifier = if (windowSizeClass == WindowWidthSizeClass.Compact) Modifier.weight(1f) else Modifier,
                title = stringResource(id = R.string.wallet_receive),
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "receive",
                onClick = onReceive,
            )
        }
        if (onBuy != null) {
            AssetAction(
                modifier = if (windowSizeClass == WindowWidthSizeClass.Compact) Modifier.weight(1f) else Modifier,
                title = stringResource(id = R.string.wallet_buy),
                imageVector = Icons.Default.Add,
                contentDescription = "buy",
                onClick = onBuy,
            )
        }
        if (onSwap != null) {
            AssetAction(
                modifier = if (windowSizeClass == WindowWidthSizeClass.Compact) Modifier.weight(1f) else Modifier,
                title = stringResource(id = R.string.wallet_swap),
                imageVector = Icons.Default.SwapVert,
                contentDescription = "swap",
                onClick = onSwap,
            )
        }
    }
}

@Composable
private fun AssetWatchOnly() {
    val uriHandler = LocalUriHandler.current
    Button(
        onClick = {},
        enabled = false,
        colors = ButtonDefaults
            .buttonColors(
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            )
            Spacer8()
            Text(
                text = stringResource(id = R.string.wallet_watch_tooltip_title),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            )
            Spacer8()
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = {
                    uriHandler.openUri(Config().getDocsUrl(DocsUrl.WHAT_IS_WATCH_WALLET))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun AssetAction(
    title: String,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(modifier = modifier, onClick = onClick, enabled = enabled) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.scrim.copy(
                            alpha = if (enabled) 1f else 0.4f,
                        ),
                        shape = CircleShape
                    )
                    .padding(16.dp),
                imageVector = imageVector,
                tint = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (enabled) 1f else 0.4f,
                ),
                contentDescription = contentDescription,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W400),
                maxLines = 1,
            )
        }
    }
}

@Preview(locale = "ru", device = Devices.PIXEL_3A)
@Composable
fun PreviewAssetHeadActions() {
    WalletTheme {
        AssetHeadActions(
            walletType = WalletType.multicoin,
            onTransfer = { },
            transferEnabled = true,
            onReceive = { },
            onBuy = {}
        ) {

        }
    }
}