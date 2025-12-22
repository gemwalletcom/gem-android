package com.gemwallet.android.ui.components.list_item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun SubheaderItem(@StringRes title: Int, vararg formatArgs: Any) {
    SubheaderItem(stringResource(title, formatArgs))
}

@Composable
fun SubheaderItem(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingDefault),
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
}