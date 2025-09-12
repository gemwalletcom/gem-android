package com.gemwallet.features.settings.develop.presents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gemwallet.features.settings.develop.viewmodels.DevelopViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun DevelopScene(
    onCancel: () -> Unit,
    viewModel: DevelopViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    Scene(
        title = stringResource(id = R.string.settings_developer),
        onClose = onCancel,
        backHandle = true,
    ) {
        LazyColumn {
            item {
                PropertyItem(
                    "Reset transactions",
                    data = ""
                ) {
                    viewModel.resetTransactions()
                }
            }
            item {
                PropertyItem("Device Id", data = viewModel.getDeviceId()) {
                    clipboardManager.setPlainText(context, viewModel.getDeviceId())
                }
                PropertyItem("Push token", data = viewModel.getPushToken().let { it.takeIf { it.isNotEmpty() } ?: "-" }) {
                    clipboardManager.setPlainText(context, viewModel.getPushToken())
                }
                PropertyItem("Store", data = viewModel.platformStore.string) {
                    clipboardManager.setPlainText(context, viewModel.platformStore.string)
                }
            }
        }
    }
}