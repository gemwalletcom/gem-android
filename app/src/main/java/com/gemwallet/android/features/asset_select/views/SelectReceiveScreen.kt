package com.gemwallet.android.features.asset_select.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.wallet.core.primitives.AssetId

@Composable
fun SelectReceiveScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: AssetSelectViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboard.current.nativeClipboard
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_receive),
        titleBadge = ::getAssetBadge,
        itemTrailing = {
            IconButton(onClick = {
                viewModel.onChangeVisibility(it.asset.id, true)
                clipboardManager.setPlainText(it.owner ?: "") //TODO: Get account address from wallet.accounts
            }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "")
            }
        },
        onCancel = onCancel,
        onSelect = onSelect,
        viewModel = viewModel,
    )
}