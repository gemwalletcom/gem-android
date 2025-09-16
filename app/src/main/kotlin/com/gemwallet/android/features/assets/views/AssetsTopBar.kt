package com.gemwallet.android.features.assets.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.features.assets.viewmodel.model.WalletInfoUIState
import com.gemwallet.android.ui.components.image.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetsTopBar(
    walletInfo: WalletInfoUIState,
    onShowWallets: () -> Unit,
    onShowAssetManage: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Box {
                TextButton(onClick = onShowWallets) {
                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        AsyncImage(model = walletInfo.icon, size = 24.dp)
                        Spacer(modifier = Modifier.Companion.size(8.dp))
                        Text(
                            text = walletInfo.name,
                            maxLines = 1,
                            overflow = TextOverflow.Companion.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "select_wallet",
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onShowAssetManage,
                Modifier.Companion.testTag("assetsManageAction")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "asset_manager",
                )
            }
        }
    )
}