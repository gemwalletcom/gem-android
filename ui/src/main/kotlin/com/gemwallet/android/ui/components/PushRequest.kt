package com.gemwallet.android.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PushRequest(
    onNotificationEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        onNotificationEnable()
        return
    }
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    if (permissionState.status.isGranted) {
        onNotificationEnable()
        onDismiss()
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Text(text = stringResource(id = R.string.notifications_permission_request_notification))
            },
            confirmButton = {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text(text = stringResource(id = R.string.common_grant_permission))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.common_no_thanks))
                }
            }
        )
    }
}