package com.gemwallet.android.features.asset_select.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun SelectReceiveScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: AssetSelectViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboardManager.current
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_receive),
        titleBadge = ::getAssetBadge,
        itemTrailing = {
            IconButton(onClick = {
                viewModel.onChangeVisibility(it.asset.id, true)
                clipboardManager.setText(AnnotatedString(it.owner))
            }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "")
            }
        },
        onCancel = onCancel,
        onSelect = onSelect,
        viewModel = viewModel,
    )
}