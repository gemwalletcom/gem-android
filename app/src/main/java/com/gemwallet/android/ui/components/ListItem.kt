package com.gemwallet.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.listItemIconSize
import com.gemwallet.android.ui.components.designsystem.padding12
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.theme.WalletTheme

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    dividerShowed: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(padding16))
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.size(padding16))
        }
        Box(modifier = Modifier
            .heightIn(72.dp)
            .weight(1f)) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = padding12, end = padding16, bottom = padding12)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    body()
                }
                if (trailing != null) {
                    Spacer(modifier = Modifier.size(padding16))
                    trailing()
                }
            }
            if (dividerShowed) {
                HorizontalDivider(Modifier.align(Alignment.BottomStart), thickness = 0.4.dp)
            }
        }
    }
}

@Composable
fun ListItem(
    iconUrl: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    supportIcon: String? = null,
    placeholder: String? = null,
    dividerShowed: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    ListItem(
        modifier = modifier,
        dividerShowed = dividerShowed,
        leading = {
            AssetIcon(modifier = iconModifier.size(listItemIconSize), iconUrl = iconUrl, placeholder = placeholder, supportIcon = supportIcon)
        },
        trailing = trailing,
        body = body,
    )
}

@Composable
fun ListItemTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    titleBadge: (@Composable () -> Unit)? = null,
    subtitle: (@Composable () -> Unit)? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
    ) {
        if (title.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                )
                titleBadge?.invoke()
            }
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            subtitle()
        }
    }
}

@Composable
fun ListItemSupportText(@StringRes stringId: Int, vararg formatArgs: Any) {
    ListItemSupportText(stringResource(stringId, *formatArgs))
}

@Composable
fun ListItemSupportText(text: String) {
    Text(
        modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun AssetIcon(
    iconUrl: String,
    placeholder: String?,
    supportIcon: String?,
    modifier: Modifier = Modifier,
) {
    Box {
        AsyncImage(
            model = iconUrl,
            placeholderText = placeholder,
            contentDescription = "list_item_icon",
            modifier = modifier,
        )
        if (!supportIcon.isNullOrEmpty()) {
            AsyncImage(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .border(0.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                model = supportIcon,
                contentDescription = "list_item_support_icon",
            )
        }
    }
}

@Composable
@Preview
fun PreviewListItem() {
    ListItem(
        leading = {
            Icon(imageVector = Icons.Default.Image, contentDescription = "")
        },
        trailing = {
            Switch(checked = false, onCheckedChange = {})
        }
    ) {
        Text(text = "Some Text")
    }
}

@Composable
@Preview
fun PreviewListItemTitle() {
    WalletTheme {
        ListItem(
            leading = {
                Icon(imageVector = Icons.Default.Image, contentDescription = "")
            },
            trailing = {
                ListItemTitle(
                    title = "89.9384 BTC",
                    subtitle = { ListItemSupportText("$100") },
                    horizontalAlignment = Alignment.End,
                )
            },
            body = {
                ListItemTitle(
                    title = "BNB Smart chain network and addition long text",
                    subtitle = { ListItemSupportText("$100") }
                )
            }
        )
    }
}