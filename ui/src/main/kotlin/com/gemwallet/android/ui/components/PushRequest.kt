package com.gemwallet.android.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PushRequest(
    showRequestDialog: Boolean = false,
    onNotificationEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    val permissionState = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        null
    } else {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = {
                if (it) {
                    onNotificationEnable()
                } else {
                    onDismiss()
                }
            }
        )
    }

    var requestNotificationPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(requestNotificationPermissions) {
        if (requestNotificationPermissions) {
            permissionState?.launchPermissionRequest()
        }
    }

    if (permissionState == null) {
        NotificationPermissionRequestDialog(onNotificationEnable, onDismiss)
    } else {
        if (permissionState.status.isGranted) {
            if (showRequestDialog) {
                NotificationPermissionRequestDialog(onNotificationEnable, onDismiss)
            } else {
                onNotificationEnable()
            }
        } else {
            requestNotificationPermissions = true
        }
    }
}

@Composable
private fun NotificationPermissionRequestDialog(
    onNotificationEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(text = stringResource(id = R.string.notifications_permission_request_notification))
        },
        confirmButton = {
            Button(onClick = onNotificationEnable) {
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