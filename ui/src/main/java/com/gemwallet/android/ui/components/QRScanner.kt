package com.gemwallet.android.ui.components

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.components.qr_scanner.screen.QRScannerScene
import com.gemwallet.android.localize.R
import com.gemwallet.android.ui.components.qr_scanner.screen.QrResultAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun qrCodeRequest(
    onCancel: CancelAction,
    onResult: QrResultAction,
): Boolean {
    var skipped by rememberSaveable { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    BackHandler(true) {
        onCancel()
    }
    return if (cameraPermissionState.status.isGranted || skipped) {
        QRScannerScene(cameraPermissionState.status.isGranted, onCancel, onResult)
        true
    } else {
        AlertDialog(
            onDismissRequest = { onCancel.invoke() },
            text = {
                Text(text = stringResource(id = R.string.camera_permission_request_camera))
            },
            confirmButton = {
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text(text = stringResource(id = R.string.common_grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = { skipped = true }) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
            }
        )
        false
    }
}