package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.models.AssetsGroupType
import com.gemwallet.android.ui.theme.paddingHalfSmall

@Composable
fun PinnedAssetsHeaderItem(type: AssetsGroupType) {
    if (type == AssetsGroupType.None) {
        return
    }
    Row(
        modifier = Modifier.Companion.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(paddingHalfSmall)
    ) {
        Icon(
            modifier = Modifier.Companion.size(16.dp),
            imageVector = when (type)  {
                AssetsGroupType.Popular -> Icons.Default.StarOutline
                AssetsGroupType.Pined -> Icons.Default.PushPin
                AssetsGroupType.None -> return
            },
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = "pinned_section",
        )
        Text(
            modifier = Modifier.Companion
                .fillMaxWidth(),
            text = stringResource(
                when (type)  {
                    AssetsGroupType.Popular -> R.string.common_popular
                    AssetsGroupType.Pined -> R.string.common_pinned
                    AssetsGroupType.None -> return
                }
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}