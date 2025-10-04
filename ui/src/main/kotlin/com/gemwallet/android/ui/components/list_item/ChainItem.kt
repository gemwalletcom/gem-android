package com.gemwallet.android.ui.components.list_item

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
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.Chain

@Composable
fun ChainItem(
    title: String,
    modifier: Modifier = Modifier,
    listPosition: ListPosition,
    icon: Any? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = {},
) {
    val modifier = onClick?.let { modifier.clickable(onClick = it) } ?: modifier
    ListItem(
        modifier = modifier.heightIn(64.dp),
        listPosition = listPosition,
        leading = @Composable {
            IconWithBadge(
                icon = (icon as? Chain)?.getIconUrl() ?: icon,
            )
        },
        title = @Composable {
            Text(
                modifier = Modifier,
                text = title.capitalize(Locale.current),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailing = if (trailing != null) {
            @Composable { trailing.invoke() }
        } else null,
    )
}

@Preview
@Composable
fun PreviewChainSelectItem() {
    MaterialTheme {
        ChainItem(icon = Chain.Polygon, listPosition = ListPosition.Middle, title = "Foo title") {}
    }
}