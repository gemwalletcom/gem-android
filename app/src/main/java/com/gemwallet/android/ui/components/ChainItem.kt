package com.gemwallet.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.assets.model.IconUrl
import com.wallet.core.primitives.Chain

@Composable
fun ChainItem(
    title: String,
    modifier: Modifier = Modifier,
    chain: Chain? = null,
    icon: IconUrl = "",
    dividerShowed: Boolean = true,
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = modifier.clickable { onClick() }.heightIn(64.dp),
        iconUrl = icon.ifEmpty { "file:///android_asset/chains/icons/${chain?.string}.png" },
        placeholder = chain?.string?.get(0).toString(),
        dividerShowed = dividerShowed,
    ) {
        Text(
            modifier = Modifier,
            text = title.capitalize(Locale.current),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
fun PreviewChainSelectItem() {
    MaterialTheme {
        ChainItem(chain = Chain.Polygon, title = "Foo title") {}
    }
}