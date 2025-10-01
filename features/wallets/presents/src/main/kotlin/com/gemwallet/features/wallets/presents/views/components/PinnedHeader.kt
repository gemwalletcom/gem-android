package com.gemwallet.features.wallets.presents.views.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.padding16
import com.gemwallet.android.ui.theme.padding8
import com.gemwallet.android.ui.theme.trailingIconSmall

internal fun LazyListScope.pinnedHeader() {
    item {
        Row(
            modifier = Modifier.padding(
                start = padding16,
                end = padding16,
                top = 24.dp,
                bottom = padding8
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(trailingIconSmall),
                imageVector = Icons.Default.PushPin,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = "pinned_section",
            )
            Spacer4()
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.common_pinned),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}