package com.gemwallet.features.asset_select.presents.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.features.asset_select.viewmodels.AssetSelectViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun SelectReceiveScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: AssetSelectViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_receive),
        titleBadge = ::getAssetBadge,
        itemTrailing = {
            IconButton(
                onClick = {
                    viewModel.onChangeVisibility(it.asset.id, true)
                    clipboardManager.setPlainText(context, viewModel.getAccount(it.asset.id)?.address ?: "")
                }
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "")
            }
        },
        onCancel = onCancel,
        onSelect = onSelect,
        viewModel = viewModel,
    )
}