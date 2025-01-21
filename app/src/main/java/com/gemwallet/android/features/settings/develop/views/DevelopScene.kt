package com.gemwallet.android.features.settings.develop.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.features.settings.develop.viewmodels.DevelopViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun DevelopScene(
    onCancel: () -> Unit,
    viewModel: DevelopViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboardManager.current
    Scene(
        title = stringResource(id = R.string.settings_developer),
        onClose = onCancel,
        backHandle = true,
    ) {
        Table(
            items = listOf(
                CellEntity("Device Id", viewModel.getDeviceId()) {
                    clipboardManager.setText(AnnotatedString(viewModel.getDeviceId()))
                },
                CellEntity("Push token", viewModel.getPushToken()) {
                    clipboardManager.setText(AnnotatedString(viewModel.getPushToken()))
                },
                CellEntity("Store", BuildConfig.FLAVOR) {
                    clipboardManager.setText(AnnotatedString(viewModel.getPushToken()))
                }
            )
        )
    }
}