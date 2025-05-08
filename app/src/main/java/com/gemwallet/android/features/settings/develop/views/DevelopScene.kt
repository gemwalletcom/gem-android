package com.gemwallet.android.features.settings.develop.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.features.settings.develop.viewmodels.DevelopViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun DevelopScene(
    onCancel: () -> Unit,
    viewModel: DevelopViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboard.current.nativeClipboard
    Scene(
        title = stringResource(id = R.string.settings_developer),
        onClose = onCancel,
        backHandle = true,
    ) {
        LazyColumn {
            item {
                PropertyItem("Device Id", viewModel.getDeviceId()) {
                    clipboardManager.setPlainText(viewModel.getDeviceId())
                }
                PropertyItem("Push token", viewModel.getPushToken()) {
                    clipboardManager.setPlainText(viewModel.getPushToken())
                }
                PropertyItem("Store", BuildConfig.FLAVOR) {
                    clipboardManager.setPlainText(BuildConfig.FLAVOR)
                }
            }
        }
    }
}