package com.gemwallet.android.ui.components.list_head

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.DisplayText
import com.gemwallet.android.ui.components.InfoBottomSheet
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.models.PriceState
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.headerIconSize
import com.gemwallet.android.ui.theme.headerSupportIconSize
import com.gemwallet.android.ui.theme.isSmallScreen
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingLarge
import com.gemwallet.android.ui.theme.paddingSmall
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.WalletType

@Composable
fun AmountListHead(
    amount: String,
    onHideBalances: (() -> Unit)? = null,
    equivalent: String? = null,
    icon: Any? = null,
    changedValue: String? = null,
    changedPercentages: String? = null,
    changeState: PriceState = PriceState.None,
    actions: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(start = paddingDefault, end = paddingDefault, bottom = paddingDefault),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            (icon as? Asset)?.let {
                HeaderIcon(it)
            } ?: IconWithBadge(icon = icon, size = headerIconSize)

            icon?.let { Spacer16() }

            DisplayText(
                text = amount,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onHideBalances != null, onClick = { onHideBalances?.invoke() })
            )
            if (!equivalent.isNullOrEmpty()) {
                Spacer4()
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
                Spacer(modifier = Modifier.size(10.dp))
                actions()
            }
        }
    }
}

@Composable
internal fun HeaderIcon(
    asset: Asset?,
    iconSize: Dp = headerIconSize,
) {
    if (asset == null) { return }
    IconWithBadge(asset, iconSize, headerSupportIconSize)
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AssetHeadActions(
    walletType: WalletType,
    transferEnabled: Boolean,
    operationsEnabled: Boolean,
    onTransfer: (() -> Unit)?,
    onReceive: (() -> Unit)?,
    onBuy: (() -> Unit)?,
    onSwap: (() -> Unit)?,
) {
    val isSmallScreen = isSmallScreen()
    if (walletType == WalletType.view) {
        AssetWatchOnly()
        return
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) paddingSmall else paddingLarge, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onTransfer != null) {
            AssetAction(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.wallet_send),
                imageVector = Icons.Default.ArrowUpward,
                enabled = transferEnabled && operationsEnabled,
                contentDescription = "send",
                onClick = onTransfer,
            )
        }
        if (onReceive != null) {
            AssetAction(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.wallet_receive),
                imageVector = Icons.Default.ArrowDownward,
                enabled = operationsEnabled,
                contentDescription = "receive",
                onClick = onReceive,
            )
        }
        if (onBuy != null) {
            AssetAction(
                modifier = Modifier.weight(1f)
                .testTag("assetBuy"),
                title = stringResource(id = R.string.wallet_buy),
                imageVector = Icons.Default.Add,
                enabled = operationsEnabled,
                contentDescription = "buy",
                onClick = onBuy,
            )
        }
        if (onSwap != null) {
            AssetAction(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.wallet_swap),
                imageVector = Icons.Default.SwapVert,
                enabled = operationsEnabled,
                contentDescription = "swap",
                onClick = onSwap,
            )
        }
    }
}

@Composable
private fun AssetWatchOnly() {
    var showInfoSheet by remember { mutableStateOf<InfoSheetEntity?>(null) }
    Button(
        onClick = { showInfoSheet = InfoSheetEntity.WatchWalletInfo },
        enabled = true,
        colors = ButtonDefaults
            .buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
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
                onClick = {  }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                )
            }
        }
    }
    InfoBottomSheet(item = showInfoSheet, onClose = { showInfoSheet = null })
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AssetAction(
    title: String,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val windowSizeClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass
    val isSmallScreen = isSmallScreen()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(paddingDefault))
            .clickable(onClick = onClick, enabled = enabled)
            .padding(vertical = paddingSmall)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(paddingSmall)
    ) {
        Icon(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = if (enabled) 1f else 0.4f,
                    ),
                    shape = CircleShape
                )
                .padding(16.dp),
            imageVector = imageVector,
            tint = MaterialTheme.colorScheme.onPrimary.copy(
                alpha = if (enabled) 1f else 0.4f,
            ),
            contentDescription = contentDescription,
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.W400,
                fontSize = if (isSmallScreen || windowSizeClass == WindowWidthSizeClass.Compact) {
                    11.sp
                } else {
                    16.sp
                }
            ),
            maxLines = 1,
        )
    }
}

@Preview(locale = "ru", device = Devices.PIXEL)
@Preview(locale = "ru", device = Devices.PIXEL_8)
@Preview(locale = "ru", device = Devices.PIXEL_9)
@Composable
fun PreviewAssetHeadActions() {
    WalletTheme {
        AssetHeadActions(
            walletType = WalletType.multicoin,
            onTransfer = { },
            transferEnabled = true,
            operationsEnabled = true,
            onReceive = { },
            onBuy = {}
        ) {

        }
    }
}