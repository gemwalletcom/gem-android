package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.type
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Currency

@Composable
fun SwapListHead(
    fromAsset: AssetInfo?,
    fromValue: String,
    toAsset: AssetInfo?,
    toValue: String,
    currency: Currency? = null,
) {
    if (fromAsset == null || toAsset == null) {
        return
    }
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding16, end = padding16, bottom = padding16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer8()
            SwapItem(assetInfo = fromAsset, value = fromValue, currency = currency)
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp),
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = ""
                )
            }
            Spacer16()
            SwapItem(assetInfo = toAsset, value = toValue, currency = currency)
        }
        Spacer8()
        HorizontalDivider(thickness = 0.4.dp)
    }
}

@Composable
private fun SwapItem(assetInfo: AssetInfo, value: String, currency: Currency?) {
    val asset = assetInfo.asset
    val decimals = asset.decimals
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = asset.format(Crypto(value), dynamicPlace = true),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
            if (currency != null) {
                Text(
                    text = currency.format(Crypto(value).convert(decimals, assetInfo.price?.price?.price ?: 0.0).atomicValue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Start
                )
            }
        }
        HeaderIcon(
            iconUrl = asset.getIconUrl(),
            supportIconUrl = if (asset.id.type() == AssetSubtype.NATIVE) null else asset.id.chain.getIconUrl(),
            placeholder = asset.id.chain.getIconUrl(),
            iconSize = 50.dp,
        )
    }
}