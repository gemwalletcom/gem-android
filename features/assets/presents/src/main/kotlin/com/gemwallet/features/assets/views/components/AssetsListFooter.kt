package com.gemwallet.features.assets.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R

@Composable
internal fun AssetsListFooter(
    onShowAssetManage: () -> Unit,
) {
    Box(
        modifier = Modifier.Companion
            .clickable(onClick = onShowAssetManage)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.Companion
                .align(Alignment.Companion.Center)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = "asset_manager",
            )
            Spacer(modifier = Modifier.Companion.size(ButtonDefaults.IconSize))
            Text(
                text = stringResource(id = R.string.wallet_manage_token_list),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}