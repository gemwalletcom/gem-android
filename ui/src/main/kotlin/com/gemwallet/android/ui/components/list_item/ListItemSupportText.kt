package com.gemwallet.android.ui.components.list_item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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