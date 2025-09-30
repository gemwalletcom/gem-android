package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.Spacer4

@Composable
fun PinnedAssetsHeaderItem() {
    Row(
        modifier = Modifier.Companion.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.Companion.size(16.dp),
            imageVector = Icons.Default.PushPin,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = "pinned_section",
        )
        Spacer4()
        Text(
            modifier = Modifier.Companion
                .fillMaxWidth(),
            text = stringResource(R.string.common_pinned),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}